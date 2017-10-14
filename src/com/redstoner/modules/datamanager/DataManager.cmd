command data {
    import [string:file] as [string:module] {
        run import_file file module;
        type console;
    }
}

command config {
    alias configs;
    alias setting;
    alias settings;
    perm datamanager.admin;
    list {
        run list;
        help Lists all modules that have at least one config setting.;
    }
    list [string:module] {
        run list2 module;
        help Lists all config settings of the specified module.;
    }
    get [string:module] [string:key] {
        run get module key;
        help Displays the value of the specified setting.;
    }
    set [string:module] [string:key] [string:value...] {
        run set module key value;
        help Assigns a new value to the given setting.;
    }
    remove_all [string:module] {
        run remove_all module;
        type console;
        help Deletes all config settings of a given module.;
    }
    remove [string:module] [string:key] {
        run remove module key;
        help Deletes the specified config setting;
    }
}