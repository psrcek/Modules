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
command me { 
    perm utils.me; 
    [string:text...] { 
        help /me's in chat.; 
        run me text; 
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
command say {
    [string:message...] { 
        perm utils.say; 
        run say message;
        help A replacement for the default say command to make the format be more consistant.; 
    } 
}
command sayn {
    [string:name] [string:message...] { 
        perm utils.sayn;
        type console;
        run sayn name message;
        help A replacement for the default say command to make the format be more consistant.; 
    }
}
command shrug {
    [string:message...] {
        perm utils.shrug;
        run shrug message;
        help Appends the shrug emoticon to the end of your message.; 
    }
}
command chat {
    alias speak;
    [string:message...] {
        perm utils.speak;
        run chat message;
        help A way to speak in normal chat with normal formatting if you have ACT or CGT on.; 
    }
}