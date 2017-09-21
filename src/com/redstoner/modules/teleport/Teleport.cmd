command teleport {
    alias eteleport;
    alias tp;
    alias etp;
    alias to;
    alias eto;
    alias tpo;
    alias etpo;
    alias tp2p;
    alias etp2p;
    [player:string] {
        run tp player;
    }
    [player:string] [player2:string] {
        run tp2 player player2;
    }
}

command teleporthere {
    alias eteleporthere;
    alias tphere;
    alias etphere;
    alias tpohere;
    alias etpohere;
    [player:string] {
        run tph player;
    }
}

command teleportask {
    alias eteleportask;
    alias tpa;
    alias etpa;
    alias tpr;
    alias etpr;
    alias tpask;
    alias etpask;
    [player:string] {
        run tpa player;
    }
}

command teleportaskhere {
    alias eteleportaskhere;
    alias tpahere,
    alias etpahere;
    alias tprhere;
    alias etrphere;
    alias tpaskhere;
    alias etpaskhere;
    [player:string] {
        run tpah player;
        help ask another player to teleport to you.;
    }
}

command tpall {
    alias etpall;
    [empty] {
        run tpall;
        help Teleports everyone to you.;
    }
    [player] {
        run tpall2 player;
        help Teleports everyone to the specified player.;
    }
    perm utils.admin.teleport;
}

command tpaall {
    alias etpall;
    [empty] {
        run tpaall;
        help Sends a tpa request to every player.;
        perm utils.admin.teleport;
    }
    [player:string] {
        run tpaall2 player;
        help Sends a tpa request to every player.;
    }
    perm utils.admin.teleport;
}

command tpaccept {
    alias etpaccept;
    alias tpyes;
    alias etpyes;
    [empty] {
        run tpaccept;
        help Accepts the latest pending tpa request.;
    }
    [index:int] {
        run tpaccept2 index;
        help Accepts the specified pending tpa request.;
    }
    perm utils.teleport.request;
}

command tpacancel {
    alias etpacencel;
    [empty] {
        run tpacancel;
        help Cancels an outgoing pending tpa request.;
        perm utils.teleport.request;
    }
}

command tpdeny {
    alias etpdeny;
    alias tpno;
    alias etpno;
    perm utils.teleport.request;
    [empty] {
        run tpdeny;
    }
    [index:int] {
        run tpdeny2 index;
    }
}

command tplist {
    alias etplist;
    alias tpl;
    alias etpl;
    [empty] {
        run tpl;
    }
}

command tptoggle {
    alias etptoggle;
    [status:string] {
        run tptoggle status;
        help sets your tpa status;
        perm utils.teleport.toggle;
    }
    [command:string] [status:string] {
        run tptoggle2 command status;
        help sets your tpa status for only one command (e.g. tpa/tpahere).;
        perm utils.teleport.toggle;
    } 
}