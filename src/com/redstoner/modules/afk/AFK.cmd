command afk {
    alias eafk;
    alias away;
    alias eaway;
    [empty] {
        run afk;
        perm utils.afk;
    }
    [string:reason] {
        run afk2 reason;
        perm utils.afk;
    }
}

command update_afk_listeners {
    [empty] {
        run update_afk_listeners;
        perm utils.afk.admin;
    }
}