command signalstrength {
    alias ss;
    perm utils.signalstrength;
    [int:strength] {
        run ss strength;
        type player;
        help "Sets the amount of items in a container to achieve the given signal strength. Uses redstone dust.";
    }
    [int:strength] [string:material] {
        run ssm strength material;
        type player;
        help "Sets the amount of itmes in a container to achieve the given signal strength. Uses the material specified.";
    }
}