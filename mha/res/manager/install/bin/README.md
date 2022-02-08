# Failover vs OnlineChange

```perl

# bin/masterha_master_switch
...

if ( $master_state eq "dead" ) {
  $exit_code = MHA::MasterFailover::main(@ARGV); # --> master_ip_failover
}
elsif ( $master_state eq "alive" ) {
  $exit_code = MHA::MasterRotate::main(@ARGV); # --> master_ip_online_change
}

...

```