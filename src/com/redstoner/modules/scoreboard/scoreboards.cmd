command project {
    [empty] {
        help Prints version info.;
        run project;
        perm utils.scoreboards;
    }
    create [flag:-s] [string:name] {
        help Creates a new Project. The -s flag indicates that you are trying to create a new sub-project instead.;
        run create -s name;
        perm utils.scoreboards.use;
    }
    delete {
        help Deletes the current project. This will also delete the entire branch attached to it, but no parents.;
        run delete;
        perm utils.scoreboards.use;
    }
    explore [string:name] {
        help Switches to the specified project. If you have multiple projects with the same name reachable in one step (example: you are in 5x5_top and want to go to 5x5_top_trigger, but you also have a project called trigger) then you need to specify the full path like this: 5x5_top_trigger;
        run explore name;
        perm utils.scoreboards.use;
    }
    hide {
        help Deselects the current project. Use the &e/project explore &7command to view it again!;
        run hide;
    }
    option {
        help Opens an interactive option menu to let you change the projects settings.;
        run optionMenu;
        perm utils.scoreboards.use;
    }
    option [string:option] {
        help Opens an interactive option menu to let you change the specified setting.;
        run optionName name;
        perm utils.scoreboards.use;
    }
    option [string:option] [string:value...] {
        help Lets you directly modify the options of a project. If no value is specified, an interactive menu opens.;
        run option name value;
        perm utils.scoreboards.use;
    }
    manage {
        help Opns an interactive menu that lets you manage the project in its entirety.;
        run manage;
        perm utils.scoreboards.use;
    }
    invite [flag:-e] [string:name] {
        help Invites a player to the current project and all sub projects. If the -e is set, subprojects will be excluded.;
        run invite e name;
        perm utils.scoreboards.use;
    }
}