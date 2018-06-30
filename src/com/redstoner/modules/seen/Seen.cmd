command seen {
     [string:player] {
        help Displays information about a player.;
        perm utils.seen;
        run seen player;
    }
    
    [string:player] [flag:ips] {
        help Displays information about a player.;
        perm utils.seen;
        run seen2 player ips;
    }
}
command firstseen {
	   [empty] {
		   run firstseen;
		   type player;
		   help Gives the date and time they first joined;
		   perm utils.firstseen;
	   }
	   [string:person] {
		   run firstseenP person;
		   help Gives the date and time when a player first joined;
		   perm utils.firstseen.other;
	   }
}
command playtime {
    [empty] {
        type player;
        run playtimeDef;
        perm utils.playtime;
        help Displays your total playtime!;
    }
    [string:name] {
        run playtime name;
        perm utils.playtime.others;
        help Displays the playtime of another player. The player must be online!;
    }
}

command uuid {
    [empty] {
        type player;
        run uuidDef;
        perm utils.uuid;
        help Displays your UUID (click to copy);
    }
    [string:name] {
        run uuid name;
        perm utils.uuid.other;
        help Displays someone elses UUID (click to copy);
    }
}