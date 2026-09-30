Add-Type -AssemblyName System.Drawing

$sourcePath = 'C:\Users\PC\Desktop\programi\injekcije\syringe_source.png'
$basePath = 'C:\Users\PC\Desktop\programi\injekcije\app\src\main\res'

$sizes = @{
    'mipmap-mdpi' = 48
    'mipmap-hdpi' = 72
    'mipmap-xhdpi' = 96
    'mipmap-xxhdpi' = 144
    'mipmap-xxxhdpi' = 192
}

$sourceImage = [System.Drawing.Image]::FromFile($sourcePath)

foreach ($folder in $sizes.Keys) {
    $size = $sizes[$folder]
    $folderPath = Join-Path $basePath $folder
    $destPath = Join-Path $folderPath 'ic_launcher.png'

    $destImage = New-Object System.Drawing.Bitmap($size, $size)
    $graphics = [System.Drawing.Graphics]::FromImage($destImage)
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.DrawImage($sourceImage, 0, 0, $size, $size)
    $graphics.Dispose()

    $destImage.Save($destPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $destImage.Dispose()

    Write-Host "Created $destPath ($size x $size)"
}

$sourceImage.Dispose()
Write-Host 'Done!'
