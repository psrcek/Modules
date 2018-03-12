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
        run config_list;
        help Lists all modules that have at least one config setting.;
    }
    list [string:module] {
        run config_list2 module;
        help Lists all config settings of the specified module.;
    }
    get [string:module] [string:key] {
        run config_get module key;
        help Displays the value of the specified setting.;
    }
    set [string:module] [string:key] [string:value...] {
        run config_set module key value;
        help Assigns a new value to the given setting.;
    }
    remove_all [string:module] {
        run config_remove_all module;
        type console;
        help Deletes all config settings of a given module.;
    }
}