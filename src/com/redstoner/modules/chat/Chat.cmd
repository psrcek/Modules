command me { 
    perm utils.me; 
    [string:text...] { 
        help /me's in chat.; 
        run me text; 
    } 
} 
command chat {
    alias speak;
    [string:message...] {
        perm utils.chat;
        run chat message;
        help A way to speak in normal chat with normal formatting if you have ACT or CGT on.; 
    }
}
command shrug {
    [string:message...] {
        perm utils.shrug;
        run shrug message;
        help Appends the shrug emoticon to the end of your message.; 
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

command mute {
    [string:player] {
        perm utils.chat.admin;
        run mute player;
        help Mutes a player.;
    }
}

command unmute {
    [string:player] {
        perm utils.chat.admin;
        run unmute player;
        help Unmutes a player.;
    }
}