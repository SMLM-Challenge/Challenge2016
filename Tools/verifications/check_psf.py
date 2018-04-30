#!/usr/bin/env python
#
# Check that the PSF in the simulations matches the reference PSF.
#
# Hazen 04/16
#

import numpy
import sys
import tifffile

if (len(sys.argv) != 4):
    print "usage: <psf.tiff> <images dir> <activations.csv>"
    exit()

#
# This assumes that the z values in the activations file are
# in the range 0 - 1500nm. We are going to calculate the average
# psf with 100nm steps in z.
#
num_z_slices = 15
psf_xy_half_size = 10
psfs = numpy.zeros((num_z_slices, 2*psf_xy_half_size, 2*psf_xy_half_size), dtype = numpy.int64)
psfs_counts = numpy.zeros(num_z_slices, dtype = numpy.int32)

image_size = 128
with open(sys.argv[3]) as act_fp:
    counts = 0
    act_fp.readline()
    for line in act_fp:
        data = line.split(",")
        f = int(data[1])
        x = round(0.01 * float(data[2]))
        y = round(0.01 * float(data[3]))
        z = int(0.01 * float(data[4]))

        if (x >= psf_xy_half_size) and (x < (image_size - psf_xy_half_size)):
            if (y >= psf_xy_half_size) and (y < (image_size - psf_xy_half_size)):
                image = tifffile.imread(sys.argv[2] + "{0:05d}.tif".format(f)).astype(numpy.int64)
                im_slice = image[y-psf_xy_half_size:y+psf_xy_half_size,x-psf_xy_half_size:x+psf_xy_half_size]
                psfs[z,:,:] += im_slice
                psfs_counts[z] += 1
                counts += 1

                if ((counts%100) == 0):
                    print "Accumulated", counts
                             
        #if (counts > 100000):
        #    break

print "Normalizing"
for i in range(num_z_slices):
    if (psfs_counts[i] > 0):
        print i, psfs_counts[i]
        psfs[i,:,:] = psfs[i,:,:]/psfs_counts[i]

psfs = psfs.astype(numpy.int16)

tifffile.imsave(sys.argv[1], psfs)


#
# The MIT License
#
# Copyright (c) 2016 Harvard Center for Advanced Imaging, Harvard University
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
#
