# One Liner

In this project I implemented a multithreaded compressor in Java.

# Performance Analysis

lshw -short
WARNING: you should run this program as super-user.
H/W path  Device    Class      Description

                    system     Computer
/0                  bus        Motherboard
/0/0                memory     64GiB System memory
/0/1                processor  Intel(R) Xeon(R) Silver 4116 CPU @ 2.10GHz
/0/2                system     PnP device PNP0b00
/1        /dev/fb0  display    hyperv_fb
/2        input0    input      AT Translated Set 2 keyboard
/3        input1    input      Microsoft Vmbus HID-compliant Mouse
/4        input2    input      PC Speaker
/5        eth0      network    Ethernet interface

lscpu
Architecture:        x86_64
CPU op-mode(s):      32-bit, 64-bit
Byte Order:          Little Endian
CPU(s):              4
On-line CPU(s) list: 0-3
Thread(s) per core:  1
Core(s) per socket:  4
Socket(s):           1
NUMA node(s):        1
Vendor ID:           GenuineIntel
CPU family:          6
Model:               85
Model name:          Intel(R) Xeon(R) Silver 4116 CPU @ 2.10GHz
Stepping:            4
CPU MHz:             2095.079
BogoMIPS:            4190.15
Hypervisor vendor:   Microsoft
Virtualization type: full
L1d cache:           32K
L1i cache:           32K
L2 cache:            1024K
L3 cache:            16896K
NUMA node0 CPU(s):   0-3
Flags:               fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush mmx fxsr sse sse2 ss ht syscall nx pdpe1gb rdtscp lm constant_tsc rep_good nopl xtopology cpuid pni pclmulqdq ssse3 fma cx16 pcid sse4_1 sse4_2 movbe popcnt aes xsave avx f16c rdrand hypervisor lahf_lm abm 3dnowprefetch invpcid_single pti ibrs ibpb stibp fsgsbase bmi1 hle avx2 smep bmi2 erms invpcid rtm mpx avx512f avx512dq rdseed adx smap clflushopt avx512cd avx512bw avx512vl xsaveopt xsavec xsaves

## Performance Comparison
programs tested:
* Pigzj under plain OpenJDK
* pigzj built with GraalVM's native-image
* /usr/local/cs/bin/pigz
* /usr/local/cs/bin/gzip
test file:
``` 
/usr/local/cs/jdk-17.0.2/lib/modules
size: 126788567 B
````
Commands:
```
> input=/usr/local/cs/jdk-17.0.2/lib/modules

>time gzip <$input >gzip.gz
output:
real    0m7.764s
user    0m7.182s
sys     0m0.073s

>time pigz <$input >pigz.gz
output:
real    0m2.471s
user    0m7.097s
sys     0m0.037s

>time java Pigzj <$input >Pigzj.gz
output:
real    0m2.788s
user    0m7.638s
sys     0m0.485s

>time ./pigzj <$input >pigzj.gz
output:
real    0m2.612s
user    0m7.341s
sys     0m0.449s
>ls -l gzip.gz pigz.gz Pigzj.gz pigzj.gz
output:
-rw-r--r-- 1 43476941 Feb 12 22:54 gzip.gz
-rw-r--r-- 1 43351345 Feb 12 22:55 pigz.gz
-rw-r--r-- 1 43352861 Feb 12 22:55 pigzj.gz
-rw-r--r-- 1 43352861 Feb 12 22:55 Pigzj.gz

> gzip -d <Pigzj.gz | cmp - $input
# output: NO OUTPUT I.E. WORKED AS EXPECTED
> gzip -d <pigzj.gz | cmp - $input
# output: NO OUTPUT I.E. WORKED AS EXPECTED
```
### Analysis

We can see both Pigzj and pigzj have performance comparable to pigz
in time.
We can also see that the compression is comparable between the 4
programs.  with the java compressors slightly outperforming gzip
but trailing pigz overall compression ratio.

## Comparing performance with variable number of threads

### Testing synopsis

packages: pigz, Pigzj, pigzj
thread number:  2, 4, 8
trials: 3 per package per thread number
metrics: real time, user time, system time, compression ratio
file used: /usr/local/cs/jdk-17.0.2/lib/modules
size: 126788567 B

pigz:
compression size: 43351345
compression ratio: 0.342

Pigzj:
compression size: 43352861
compression ratio: 0.342

pigzj:
compression size: 43352861
compression ratio:0.342

2 threads performance:
pigz
command: `time pigz -p 2 <$input >pigz.gz`
trial 1:
real    0m4.017s
user    0m7.052s
sys     0m0.080s

trial 2:
real    0m4.126s
user    0m7.022s
sys     0m0.095s

trial 3:
real    0m4.011s
user    0m7.034s
sys     0m0.093s



Pigzj
command: `time java Pigzj -p 2 <$input >Pigzj.gz`
trial 1:
real    0m4.330s
user    0m7.436s
sys     0m0.509s
trial 2:
real    0m4.298s
user    0m7.486s
sys     0m0.504s
trial 3:
real    0m6.517s
user    0m7.462s
sys     0m0.510s



pigzj
command: `time ./pigzj -p 2 <$input >pigzj.gz`
trial 1:
real    0m4.071s
user    0m7.289s
sys     0m0.507s
trial 2:
real    0m4.109s
user    0m7.310s
sys     0m0.493s
trial 3:
real    0m4.178s
user    0m7.317s
sys     0m0.495s

4 threads performance:
pigz
command: `time pigz -p 4 <$input >pigz.gz`
trial 1:
real    0m2.746s
user    0m7.086s
sys     0m0.041s

trial 2:
real    0m2.724s
user    0m7.092s
sys     0m0.045s

trial 3:
real    0m2.781s
user    0m7.083s
sys     0m0.051s


Pigzj
command: `time java Pigzj -p 4 <$input >Pigzj.gz`
trial 1:
real    0m3.065s
user    0m7.442s
sys     0m0.478s
trial 2:
real    0m3.122s
user    0m7.462s
sys     0m0.445s
trial 3:
real    0m3.138s
user    0m7.445s
sys     0m0.474s

pigzj
command: `time ./pigzj -p 4 <$input >pigzj.gz`
trial 1:
real    0m2.879s
user    0m7.311s
sys     0m0.474s
trial 2:
real    0m3.009s
user    0m7.300s
sys     0m0.477s
trial 3:
real    0m2.924s
user    0m7.314s
sys     0m0.500s

8 threads performance:
pigz
command: `time pigz -p 8 <$input >pigz.gz`
trial 1:
real    0m2.817s
user    0m7.079s
sys     0m0.061s

trial 2:
real    0m2.819s
user    0m7.075s
sys     0m0.061s
trial 3:
real    0m2.698s
user    0m7.077s
sys     0m0.059s

Pigzj
command:  `time java Pigzj -p 8 <$input >Pigzj.gz`
trial 1:
real    0m3.152s
user    0m7.459s
sys     0m0.459s

trial2:
real    0m3.204s
user    0m7.456s
sys     0m0.472s

trial 3:
real    0m3.215s
user    0m7.466s
sys     0m0.445s

pigzj
command: `time ./pigzj -p 8 <$input >pigzj.gz`
trial 1:
real    0m3.110s
user    0m7.345s
sys     0m0.409s

trial 2:
real    0m3.198s
user    0m7.404s
sys     0m0.437s
trial 3:
real    0m2.879s
user    0m7.347s
sys     0m0.426s


### Analysis 

Overall the performance for each of pigz, Pigzj, and pigzj were comparable
for all number of threads.  pigz slightly outperformed the java
implementations at each level.
4 threads was faster than 2 but the performance gains leveled out after 4
probably due to the fact that the machine I tested on only had 4 available
processors.

### strace

When I did my strace analysis, I noticed that my pigzj and Pigzj implementations
made more write system calls than the pigz implementation.  They probably took
advantage of doing buffered writes which are less expensive and therefore their
performance was better.

### problems with scale

I think that my the differences in performance between my java implementations and
pigz will only get more pronounced as the file sizes gets larger.  There
would be areas to optimize my code so that it runs faster.  namely I could take
advantage of more buffered writes. I could also have a thread that is just writing
futures as they appear, so I am not writing serially and not waiting for all the
compression to be finished before i begin writing.  This would really make a difference
with bigger files.

I think as the number of threads scale, as long as there are enough processors that
will improve performance.  If however, we use more threads than processors available
this will hurt performance because context switching between threads is expensive.
