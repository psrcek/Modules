command vanish { 
    [empty] { 
        help Toggles your vanish status.; 
        type player; 
        run vanish; 
        perm utils.vanish; 
    } 
    on { 
        help Turns your vanish on.; 
        type player; 
        run vanish_on; 
        perm utils.vanish; 
    } 
    off { 
        help Turns your vanish off.; 
        type player; 
        run vanish_off; 
        perm utils.vanish; 
    } 
    [string:name] { 
        help Toggles someone elses vanish; 
        run vanish_other name; 
        perm utils.vanishother; 
    } 
}
command imout { 
	[empty] { 
		help Makes you magically disappear; 
		type player; 
		perm utils.imout; 
		run imout; 
	} 
}