####About
`ab -n 2000 -c 200 "http://localhost:8090/"`

Mini test

Server Software:        Jetty(9.4.7.v20170914)
Server Hostname:        localhost
Server Port:            8090

Document Path:          /
Document Length:        0 bytes

Concurrency Level:      200
Time taken for tests:   55.087 seconds
Complete requests:      2000
Failed requests:        0
Total transferred:      176000 bytes
HTML transferred:       0 bytes
Requests per second:    36.31 [#/sec] (mean)
Time per request:       5508.651 [ms] (mean)
Time per request:       27.543 [ms] (mean, across all concurrent requests)
Transfer rate:          3.12 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    1   1.8      0      10
Processing:  5000 5005  12.2   5001    5064
Waiting:     5000 5005  12.2   5001    5064
Total:       5000 5006  13.8   5001    5073