#!/bin/bash
bitrate=20M
preset=veryslow
set -e

for i in $( ls *.mp4 ); do
    echo "============================="
    echo $i
    echo "Pass 1..."
    ffmpeg -y -i $i -c:v libx264 -preset $preset -b:v $bitrate -pass 1 -f mp4 /dev/null
    echo "-----------------------------"
    echo "Pass 2..."
    ffmpeg -i $i -c:v libx264 -preset $preset -b:v $bitrate -pass 2 compressed-2pass-$bitrate-$preset-$i
done
