command list {
    alias ls;
    alias elist;
    alias online;
    alias eonline;
    alias playerlist;
    alias eplayerlist;
    alias plist;
    alias eplist;
    alias who;
    alias ewho;
    [empty] {
        run list;
        help Shows all online players sorted by rank.;
    }
    [string:rank...] {
        run list_rank rank;
        help Shows all online players of the specified rank(s);
    }
}
command staff {
    [empty] {
        help Shows all online staff.;
        run staff;
    }
}
command console_join {
    [string:name] {
        run console_join name;
        type console;
    }
}
command console_leave {
    [string:name] {
        run console_leave name;
        type console;
    }
}
