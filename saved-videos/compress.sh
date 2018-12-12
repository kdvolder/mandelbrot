#!/bin/bash
bitrate=20M
preset=veryslow
set -e
echo "Pass 1..."
ffmpeg -y -i $1 -c:v libx264 -preset $preset -b:v $bitrate -pass 1 -f mp4 /dev/null
echo "Pass 2..."
ffmpeg -i $1 -c:v libx264 -preset $preset -b:v $bitrate -pass 2 compressed-2pass-$bitrate-$preset-$1
