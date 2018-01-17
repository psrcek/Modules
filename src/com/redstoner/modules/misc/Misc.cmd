command tempadd { 
    perm pex; 
    [string:user] [string:group] { 
        help Adds a user to a group for 1w.; 
        run tempadddef user group; 
    } 
    [string:user] [string:group] [string:duration] { 
        help Adds a user to a group for a specified duration.; 
        run tempadd user group duration; 
    } 
} 
command echo { 
    [string:text...] { 
        help Echoes back to you.; 
        run echo text; 
    } 
} 
command ping { 
    [empty] { 
        help Pongs :D; 
        run ping; 
    } 
    [string:password] { 
        help Pongs :D; 
        run ping2 password; 
    } 
} 
command sudo { 
    perm utils.sudo; 
    [string:name] [string:command...] { 
        help Sudo'es another user (or console); 
        run sudo name command; 
    } 
} 
command hasperm {
    [flag:-f] [string:name] [string:node] { 
        perm utils.hasperm; 
        run hasperm -f name node;
        help Checks if a player has a given permission node or not. Returns \"true/false\" in chat. When -f is set, it returns it unformatted.; 
    } 
}
command nightvision {
alias nv; 
	[empty] {
		run illuminate; 
		type player;
		help Gives the player infinte night vision; 
		perm utils.illuminate; 
	} 
}