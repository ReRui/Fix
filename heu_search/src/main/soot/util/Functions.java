package soot.util;

import soot.PhaseOptions;
import soot.Scene;
import soot.options.Options;

import java.util.Collections;

public class Functions {

    public static void initSoot(String path) {
        soot.G.reset();

        Options options = Options.v();
        //allow soot class creation from missing classes
        options.set_allow_phantom_refs(true);

        //prepend the given soot classpath to the default classpath
        options.set_prepend_classpath(true);

        // run internal validation on bodies
        options.set_validate(true);

        // set output format for soot
        options.set_output_format(Options.output_format_jimple);

        // only java are accepted by soot analysis
        options.set_src_prec(Options.src_prec_java);

        // keep line number table, so you can access line number when later analysis by
        options.set_keep_line_number(true);

        // attach bytecode offset to IR, we don't need this feature this project
        //options.set_keep_offset(true);

        // do not load bodies for excluded classes
        options.set_no_bodies_for_excluded(true);

        //options.set_whole_program(true);

        //options.set_soot_classpath(Scene.v().defaultClassPath() + ";" + path);

        // process all classes found in dir
        options.set_process_dir(Collections.singletonList(path));

        // use original names in jimple in phase Jimple build
        PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");

        // load neccessary classes for later analysis
        Scene.v().loadNecessaryClasses();
    }
}
