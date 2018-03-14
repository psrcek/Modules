command afk {
    alias eafk;
    alias away;
    alias eaway;
    [empty] {
        run afk;
        perm utils.afk;
    }
    [optional:-s] {
        run afks -s;
        perm utils.afk;
    }
    [optional:-s] [string:reason...] {
        run afk2 -s reason;
        perm utils.afk;
    }
}

command update_afk_listeners {
    [empty] {
        run update_afk_listeners;
        perm utils.afk.admin;
    }
}