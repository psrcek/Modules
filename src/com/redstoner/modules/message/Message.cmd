command message {
    [string:player] [string:message...] {
        run message player message;
        help Sends a direct message to a player.;
        perm utils.message;
    }
}

command reply {
    [string:message...] {
        run reply message;
        help Sends a direct message to the last person you talked to.;
        perm utils.message;
    }
}

command socialspy {
    format {
        run config_format_default;
        help Resets your format back to the default: &e%s;
    }
    format [string:format...] {
        run config_format format;
        help Specifies your ss format. Use /socialspy format_help to get info about how the format works.;
    }
    format_help {
        run format_help;
        help Displays info about the format command;
    }
    prefix {
        run config_prefix_default;
        help Resets your color back to the default (light gray color code);
    }
    prefix [string:prefix] {
        run config_prefix prefix;
        help Sets your prefix to the specified term.;
    }
    stripcolor on {
        run stripcolor_on;
    }
    stripcolor off {
        run stripcolor_off;
    }
    stripcolor {
        run stripcolor;
    }
    on {
        run on;
    }
    off {
        run off;
    }   
    [empty] {
        run toggle;
    }
    perm utils.ss;
    type player;
}