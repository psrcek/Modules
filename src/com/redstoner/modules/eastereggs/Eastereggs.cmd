command rekmedaddy {
    [empty] {
        run rekmedaddy;
        help Rek me harder;
    }
}
command shear {
    [empty] {
        run shear;
        help Sets your shear item to the item in your hand.;
    }
    clear {
        run shear_clear;
        help Resets your shear item;
    }
    perm utils.eastereggs.shear;
    type player;
}
command stick {
    [empty] {
        run stick;
        help Does stuff.;
        type player;
    }
}
command hidden {
    [empty] {
        run hidden;
        help Does stuff.;
    }
}

command remindmedaddy {
    [empty] {
        run remind;
        help Does stuff.;
    }
}

command deadbush {
    [empty] {
        run bush;
        help Does stuff.;
    }
}

command /grief {
    [empty] {
        run grief;
        help Does stuff.;
    }
}