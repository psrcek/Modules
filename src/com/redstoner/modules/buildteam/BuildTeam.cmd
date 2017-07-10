command teleport {
    alias tp;
    alias tele;
    [string:player...] {
        run teleport player;
    }
    type player;
}

command team_add {
    [string:player] {
        run team_add player;
        perm utils.buildteam.manage;
    }
}

command team_remove {
    [string:player] {
        run team_remove player;
        perm utils.buildteam.manage;
    }
}
