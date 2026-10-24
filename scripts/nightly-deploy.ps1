$ErrorActionPreference = "Stop"

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message"
}

function Invoke-GitHubAPI {
    param(
        [string]$Endpoint,
        [string]$Method = "GET",
        [object]$Body = $null,
        [string]$ContentType = "application/json"
    )

    $headers = @{
        "Authorization" = "token $env:GITHUB_TOKEN"
        "Accept" = "application/vnd.github.v3+json"
        "User-Agent" = "PowerShell-CI-Script"
    }

    try {
        $params = @{
            Uri = "https://api.github.com/$Endpoint"
            Method = $Method
            Headers = $headers
            ContentType = $ContentType
        }

        if ($Body) {
            if ($ContentType -eq "application/json" -and $Body -isnot [string]) {
                $params.Body = $Body | ConvertTo-Json -Depth 10 -Compress
            }
            else {
                $params.Body = $Body
            }
        }

        $response = Invoke-RestMethod @params
        return $response
    }
    catch {
        $errorDetails = $_.Exception.Message
        try {
            $responseStream = $_.Exception.Response.GetResponseStream()
            if ($responseStream) {
                $reader = New-Object System.IO.StreamReader($responseStream)
                $errorBody = $reader.ReadToEnd()
                $reader.Close()
                $errorDetails += " - Response: $errorBody"
            }
        }
        catch {}

        Write-Log "GitHub API调用失败 ($Method $Endpoint): $errorDetails"
        throw
    }
}

function UploadToRelease {
    param(
        [string]$ReleaseUrl,
        [string]$FilePath,
        [string]$AssetName
    )

    Write-Log "开始上传文件: $AssetName"

    if ($ReleaseUrl -match "releases/tag/([^/]+)$") {
        $tagName = $matches[1]
        $releaseInfo = Invoke-GitHubAPI -Endpoint "repos/$env:APPVEYOR_REPO_NAME/releases/tags/$tagName"
        $releaseId = $releaseInfo.id
    }
    elseif ($ReleaseUrl -match "releases/(\d+)$") {
        $releaseId = $matches[1]
    }
    else {
        throw "无法从Release链接提取ID: $ReleaseUrl"
    }

    if (-not (Test-Path $FilePath)) {
        throw "文件不存在: $FilePath"
    }

    $uploadUrl = "https://uploads.github.com/repos/$env:APPVEYOR_REPO_NAME/releases/$releaseId/assets"
    $queryParams = "?name=$([System.Web.HttpUtility]::UrlEncode($AssetName))"

    $fileBytes = [System.IO.File]::ReadAllBytes($FilePath)
    $contentType = "application/java-archive"

    $headers = @{
        "Authorization" = "token $env:GITHUB_TOKEN"
        "Accept" = "application/vnd.github.v3+json"
        "Content-Type" = $contentType
    }

    try {
        Write-Log "上传文件到: $uploadUrl$queryParams"
        $response = Invoke-RestMethod -Uri "$uploadUrl$queryParams" `
            -Method POST `
            -Headers $headers `
            -Body $fileBytes `
            -ContentType $contentType
        Write-Log "文件上传成功: $($response.name) ($($response.size) bytes)"
        return $response
    }
    catch {
        Write-Log "文件上传失败: $($_.Exception.Message)"
        throw
    }
}

function Remove-ReleaseAsset {
    param(
        [string]$AssetId
    )

    try {
        Write-Log "删除资产: $AssetId"
        Invoke-GitHubAPI -Endpoint "repos/$env:APPVEYOR_REPO_NAME/releases/assets/$AssetId" -Method DELETE
        Write-Log "资产删除成功"
    }
    catch {
        Write-Log "资产删除失败: $($_.Exception.Message)"
    }
}

Write-Log "开始部署处理..."

if (-not (Test-Path $env:NIGHTLY_FILE_PATH)) {
    Write-Log "错误: 构建产物文件不存在: $env:NIGHTLY_FILE_PATH"
    exit 1
}

Write-Log "部署配置:"
Write-Log "  Release链接: $env:DEPLOYMENT_LINK"
Write-Log "  目标分支: $env:APPVEYOR_REPO_BRANCH"
Write-Log "  构建产物: $env:NIGHTLY_FILE_PATH"

Write-Log "获取Release信息..."
try {
    if ($env:DEPLOYMENT_LINK -match "github\.com/([^/]+/[^/]+)/releases") {
        $repoPath = $matches[1]
        if ($repoPath -ne $env:APPVEYOR_REPO_NAME) {
            Write-Log "警告: Release链接的仓库 ($repoPath) 与当前仓库 ($env:APPVEYOR_REPO_NAME) 不匹配"
        }
    }

    $releaseUrl = $env:DEPLOYMENT_LINK
    if ($releaseUrl -match "releases/tag/([^/]+)$") {
        $tagName = $matches[1]
        $release = Invoke-GitHubAPI -Endpoint "repos/$env:APPVEYOR_REPO_NAME/releases/tags/$tagName"
    }
    else {
        $release = Invoke-GitHubAPI -Endpoint "repos/$env:APPVEYOR_REPO_NAME/releases/latest"
    }

    $releaseId = $release.id
    $assets = $release.assets

    Write-Log "找到Release: $($release.name) (ID: $releaseId)"
    Write-Log "当前资产数量: $($assets.Count)"
}
catch {
    Write-Log "获取Release信息失败: $($_.Exception.Message)"
    exit 1
}

Write-Log "清理旧的分支构建产物..."
$branchPrefix = $env:APPVEYOR_REPO_BRANCH -replace '[^a-zA-Z0-9._-]', '_'
$deletedCount = 0

foreach ($asset in $assets) {
    $assetName = $asset.name
    if ($assetName -like "$branchPrefix*") {
        Write-Log "找到需要删除的旧文件: $assetName"
        try {
            Remove-ReleaseAsset -AssetId $asset.id
            $deletedCount++
        }
        catch {
            Write-Log "删除文件失败: $assetName - $_"
        }
    }
}

Write-Log "清理完成，删除了 $deletedCount 个旧文件"

$originalFileName = [System.IO.Path]::GetFileName($env:NIGHTLY_FILE_PATH)
$branchSafeName = $env:APPVEYOR_REPO_BRANCH -replace '[^a-zA-Z0-9._-]', '_'
$newAssetName = "$branchSafeName-$originalFileName"

Write-Log "新文件名: $newAssetName"

$tempDir = Join-Path $env:TEMP "github-deploy-$(Get-Date -Format 'yyyyMMddHHmmss')"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

$tempFilePath = Join-Path $tempDir $newAssetName
try {
    Write-Log "创建临时副本: $tempFilePath"
    Copy-Item -Path $env:NIGHTLY_FILE_PATH -Destination $tempFilePath -Force

    if (-not (Test-Path $tempFilePath)) {
        throw "文件复制失败"
    }

    $fileSize = (Get-Item $tempFilePath).Length
    Write-Log "文件复制成功，大小: $fileSize bytes"
}
catch {
    Write-Log "创建临时副本失败: $($_.Exception.Message)"
    if (Test-Path $tempDir) {
        Remove-Item -Path $tempDir -Recurse -Force -ErrorAction SilentlyContinue
    }
    exit 1
}

try {
    Write-Log "开始上传文件到GitHub Release..."
    $uploadResult = UploadToRelease -ReleaseUrl $env:DEPLOYMENT_LINK `
        -FilePath $tempFilePath `
        -AssetName $newAssetName
    Write-Log "上传成功!"
    Write-Log "文件URL: $($uploadResult.browser_download_url)"
    Write-Log "文件大小: $($uploadResult.size) bytes"

    $env:DEPLOYED_FILE_URL = $uploadResult.browser_download_url
    $env:DEPLOYED_FILE_NAME = $newAssetName
    $exitCode = 0
}
catch {
    Write-Log "上传失败: $($_.Exception.Message)"
    $exitCode = 1
}
finally {
    Write-Log "清理临时文件..."
    if (Test-Path $tempDir) {
        Remove-Item -Path $tempDir -Recurse -Force -ErrorAction SilentlyContinue
        Write-Log "临时目录已清理"
    }
}

if ($exitCode -eq 0) {
    Write-Log "部署完成!"
}
else {
    Write-Log "部署失败"
    exit $exitCode
}
