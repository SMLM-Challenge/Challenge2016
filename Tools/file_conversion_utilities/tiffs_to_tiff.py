#!/usr/bin/env python
#
# Convert a mess of tiff files into a single multi-page tiff file.
#
# Hazen 04/16
#

import glob
import sys
import tifffile

if (len(sys.argv) != 3):
    print("usage: <tiff file> <tiff dir>")
    exit()

tiff_files = sorted(glob.glob(sys.argv[2] + "*.tif"))

with tifffile.TiffWriter(sys.argv[1]) as tif:
    for tiff_file in tiff_files:
        print(tiff_file)
        tiff_image = tifffile.imread(tiff_file)
        tif.save(tiff_image)
