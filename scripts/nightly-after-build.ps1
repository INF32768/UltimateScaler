$ErrorActionPreference = "Stop"

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message"
}

Write-Log "开始重命名构建产物..."

if (-not $env:PROJECT_NAME -or -not $env:VERSION) {
    Write-Log "错误: PROJECT_NAME 或 VERSION 环境变量未设置"
    exit 1
}

$buildDate = Get-Date -Format "yyyyMMdd"
$shortSha = $env:APPVEYOR_REPO_COMMIT.Substring(0, 8)
$nightlyFileName = "{0}-nightly-{1}-{2}-{3}.jar" -f $env:PROJECT_NAME, $env:VERSION, $buildDate, $shortSha
$env:NIGHTLY_FILENAME = $nightlyFileName

$sourceDir = "build\libs"
$originalFileName = "{0}-{1}.jar" -f $env:PROJECT_NAME, $env:VERSION
$sourceFilePath = Join-Path $sourceDir $originalFileName
$targetFilePath = Join-Path $sourceDir $env:NIGHTLY_FILENAME

if (-not (Test-Path $sourceFilePath)) {
    Write-Log "警告: 原始文件不存在: $sourceFilePath"
    $possibleFiles = Get-ChildItem -Path $sourceDir -Filter "*.jar" | Where-Object {
        $_.Name -match "^$([regex]::Escape($env:PROJECT_NAME))-$([regex]::Escape($env:VERSION))"
    }

    if ($possibleFiles.Count -eq 0) {
        Write-Log "错误: 未找到任何匹配的构建产物"
        exit 1
    }

    $mainJar = $possibleFiles | Where-Object {
        $_.Name -match "^$([regex]::Escape($env:PROJECT_NAME))-$([regex]::Escape($env:VERSION))\.jar$"
    }

    if ($mainJar) {
        $sourceFilePath = $mainJar.FullName
        Write-Log "找到主JAR文件: $sourceFilePath"
    }
    else {
        $sourceFilePath = $possibleFiles[0].FullName
        Write-Log "使用找到的JAR文件: $sourceFilePath"
    }
}

if (-not (Test-Path $sourceDir)) {
    Write-Log "错误: 构建目录不存在: $sourceDir"
    exit 1
}

try {
    Write-Log "重命名文件:"
    Write-Log "  从: $(Split-Path $sourceFilePath -Leaf)"
    Write-Log "  到: $(Split-Path $targetFilePath -Leaf)"

    if (Test-Path $targetFilePath) {
        Remove-Item -Path $targetFilePath -Force
        Write-Log "已删除已存在的目标文件"
    }

    Rename-Item -Path $sourceFilePath -NewName $env:NIGHTLY_FILENAME -Force
    Write-Log "文件重命名成功"

    $symlinkPath = Join-Path $sourceDir $originalFileName
    if (-not (Test-Path $symlinkPath)) {
        try {
            New-Item -ItemType SymbolicLink -Path $symlinkPath -Target $targetFilePath -Force | Out-Null
            Write-Log "已创建符号链接: $originalFileName -> $env:NIGHTLY_FILENAME"
        }
        catch {
            Write-Log "警告: 无法创建符号链接: $_"
        }
    }

    $env:NIGHTLY_FILE_PATH = $targetFilePath
    Write-Log "设置文件路径变量: $targetFilePath"
}
catch {
    Write-Log "重命名文件失败: $($_.Exception.Message)"
    exit 1
}

Write-Log "构建后处理完成"
