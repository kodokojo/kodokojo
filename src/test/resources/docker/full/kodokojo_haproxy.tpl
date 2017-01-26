global
  maxconn 4096
  log {{ .SyslogEntryPoint }}   local0
  log {{ .SyslogEntryPoint }}   local1 info

defaults

  mode http
  log global
  option httplog
  option  http-server-close
  option  dontlognull
  option  redispatch
  option  contstats
  retries 3
  backlog 10000
  timeout client          25s
  timeout connect          5s
  timeout server          25s
  timeout tunnel        3600s
  timeout http-keep-alive  1s
  timeout http-request    15s
  timeout queue           30s
  timeout tarpit          60s
  default-server inter 3s rise 2 fall 3
  option forwardfor


frontend http-in
  log     global
  mode    http
  bind *:80
  reqadd X-Forwarded-Proto:\ http

frontend https-in
  log     global
  mode    http
  bind *:443 ssl crt /usr/local/etc/haproxy/ssl/kodokojo-server.pem
  reqadd X-Forwarded-Proto:\ https

# Source : http://blog.haproxy.com/2012/11/07/websockets-load-balancing-with-haproxy/

  acl host_ws hdr_beg(Host) -i ws.
  use_backend back-cluster-ws if host_ws

  acl hdr_connection_upgrade hdr(Connection)  -i upgrade
  acl hdr_upgrade_websocket  hdr(Upgrade)     -i websocket

  use_backend back-cluster-ws if hdr_connection_upgrade hdr_upgrade_websocket
  default_backend ui-cluster-http

  stats enable
  stats auth admin:admin
  stats uri /stats

backend ui-cluster-http
  balance roundrobin
  mode    http
  redirect scheme https if !{ ssl_fc }
  balance leastconn
{{range .Projects}}{{$projectConfigurationId := .ProjectName}}{{if .IsReady}}{{range .HaProxyHTTPEntries}}{{if eq "ui" .EntryName}}
{{range $index,$backend := .Backends}}  server ui{{$index}} {{$backend.BackEndHost}}:{{$backend.BackEndPort}} check
{{end}}
{{end}}{{end}}{{end}}{{end}}

backend back-cluster-ws
  balance roundrobin

  ## websocket protocol validation
  acl hdr_connection_upgrade hdr(Connection)                 -i upgrade
  acl hdr_upgrade_websocket  hdr(Upgrade)                    -i websocket
  acl hdr_websocket_key      hdr_cnt(Sec-WebSocket-Key)      eq 1
  acl hdr_websocket_version  hdr_cnt(Sec-WebSocket-Version)  eq 1
  http-request deny if ! hdr_connection_upgrade ! hdr_upgrade_websocket ! hdr_websocket_key ! hdr_websocket_version

  ## ensure our application protocol name is valid
  ## (don't forget to update the list each time you publish new applications)
  acl ws_valid_protocol hdr(Sec-WebSocket-Protocol) echo-protocol
  http-request deny if ! ws_valid_protocol

  ## websocket health checking
  option httpchk GET / HTTP/1.1\r\nHost:\ lb.kodokojo.io\r\nConnection:\ Upgrade\r\nUpgrade:\ websocket\r\nSec-WebSocket-Key:\ haproxy\r\nSec-WebSocket-Version:\ 13\r\nSec-WebSocket-Protocol:\ echo-protocol
  http-check expect status 101
{{range .Projects}}{{$projectConfigurationId := .ProjectName}}{{if .IsReady}}{{range .HaProxyHTTPEntries}}{{if eq "back" .EntryName}}
{{range $index,$backend := .Backends}}  server back{{$index}} {{$backend.BackEndHost}}:{{$backend.BackEndPort}} maxconn 30000 weight 10 cookie back{{$index}} check
{{end}}
{{end}}{{end}}{{end}}{{end}}

