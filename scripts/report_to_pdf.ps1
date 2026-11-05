# 把报告 docx 导出成 PDF（用于目视检查排版）
#
# 用法：powershell -File scripts/report_to_pdf.ps1 -In <docx> -Out <pdf>
#
# 经验（踩过的坑）：
#   1. 直接打开 docs/report 下的原文件，Word 有时判定「文档已损坏」或卡住 ——
#      先拷到 %TEMP% 的纯英文路径再打开就正常。
#   2. 导出目标也必须是纯英文路径，导出到中文文件名会卡死不出结果，
#      所以先导到临时 pdf，再由调用方拷回去。

param(
    [Parameter(Mandatory = $true)][string]$In,
    [Parameter(Mandatory = $true)][string]$Out
)

$ErrorActionPreference = "Stop"
if (-not (Test-Path $In)) { throw "找不到输入文件：$In" }
if (Test-Path $Out) { Remove-Item $Out -Force }

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0

try {
    $doc = $word.Documents.Open($In, $false, $true)
    $doc.ExportAsFixedFormat($Out, 17)
    Write-Host ("已导出 PDF，页数：" + $doc.ComputeStatistics(2))
    $doc.Close($false)
}
finally {
    $word.Quit()
}
