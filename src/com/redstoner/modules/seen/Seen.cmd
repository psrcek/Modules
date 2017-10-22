command seen {
    [string:player] [flag:ips] {
        help Displays information about a player.;
        perm utils.seen;
        run seen2 player ips;
    }
}