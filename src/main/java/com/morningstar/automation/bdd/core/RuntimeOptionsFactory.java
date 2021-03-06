package com.morningstar.automation.bdd.core;

import cucumber.api.CucumberOptions;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.io.MultiLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

import java.lang.reflect.Method;

public class RuntimeOptionsFactory {
    private final Class clazz;
    private final Method method;
    private boolean featuresSpecified = false;
    private boolean glueSpecified = false;
    private boolean pluginSpecified = false;

    public RuntimeOptionsFactory(Class clazz, Method method) {
        this.clazz = clazz;
        this.method = method;
    }

    public RuntimeOptions create() {
        List<String> args = buildArgsFromOptions();
        List<String> methodArgs = buildArgsFromMethod();
        args.addAll(methodArgs);
        return new RuntimeOptions(args);
    }
    
    private List<String> buildArgsFromMethod() {
    	List<String> args = new ArrayList<String>();
    	String[] scenarioFilterTags = null;
    	String[] scenarioFilterName = null;
    	ScenarioFilter annotation = method.getAnnotation(ScenarioFilter.class);
		if (annotation != null) {
			scenarioFilterTags = annotation.tags();
			scenarioFilterName = annotation.name();
		}
		this.addToArgs(args, scenarioFilterTags, "--tags");
		this.addToArgs(args, scenarioFilterName, "--name");
    	return args;
    }
    
    private void addToArgs(List<String> args, String[] values, String key){
    	if(values != null && values.length > 0){
    		for (String value : values) {
                args.add(key);
                args.add(value);
            }
    	}
    }

    private List<String> buildArgsFromOptions() {
        List<String> args = new ArrayList<String>();

        for (Class classWithOptions = clazz; hasSuperClass(classWithOptions); classWithOptions = classWithOptions.getSuperclass()) {
            CucumberOptions options = getOptions(classWithOptions);
            if (options != null) {
                addDryRun(options, args);
                addMonochrome(options, args);
                addTags(options, args);
                addPlugins(options, args);
                addStrict(options, args);
                addName(options, args);
                addSnippets(options, args);
                addGlue(options, args);
                addFeatures(options, args);
                addJunitOptions(options, args);
            }
        }
        addDefaultFeaturePathIfNoFeaturePathIsSpecified(args, clazz);
        addDefaultGlueIfNoGlueIsSpecified(args, clazz);
        addNullFormatIfNoPluginIsSpecified(args);
        return args;
    }

    private void addName(CucumberOptions options, List<String> args) {
        for (String name : options.name()) {
            args.add("--name");
            args.add(name);
        }
    }

    private void addSnippets(CucumberOptions options, List<String> args) {
        args.add("--snippets");
        args.add(options.snippets().toString());
    }

    private void addDryRun(CucumberOptions options, List<String> args) {
        if (options.dryRun()) {
            args.add("--dry-run");
        }
    }

    private void addMonochrome(CucumberOptions options, List<String> args) {
        if (options.monochrome() || runningInEnvironmentWithoutAnsiSupport()) {
            args.add("--monochrome");
        }
    }

    private void addTags(CucumberOptions options, List<String> args) {
        for (String tags : options.tags()) {
            args.add("--tags");
            args.add(tags);
        }
    }

    private void addPlugins(CucumberOptions options, List<String> args) {
        List<String> plugins = new ArrayList<String>();
        plugins.addAll(asList(options.plugin()));
        plugins.addAll(asList(options.format()));
        for (String plugin : plugins) {
            args.add("--plugin");
            args.add(plugin);
            if (PluginFactory.isFormatterName(plugin)) {
                pluginSpecified = true;
            }
        }
    }

    private void addNullFormatIfNoPluginIsSpecified(List<String> args) {
        if (!pluginSpecified) {
            args.add("--plugin");
            args.add("null");
        }
    }

    private void addFeatures(CucumberOptions options, List<String> args) {
        if (options != null && options.features().length != 0) {
            Collections.addAll(args, options.features());
            featuresSpecified = true;
        }
    }

    private void addDefaultFeaturePathIfNoFeaturePathIsSpecified(List<String> args, Class clazz) {
        if (!featuresSpecified) {
            args.add(MultiLoader.CLASSPATH_SCHEME + packagePath(clazz));
        }
    }

    private void addGlue(CucumberOptions options, List<String> args) {
        for (String glue : options.glue()) {
            args.add("--glue");
            args.add(glue);
            glueSpecified = true;
        }
    }

    private void addDefaultGlueIfNoGlueIsSpecified(List<String> args, Class clazz) {
        if (!glueSpecified) {
            args.add("--glue");
            args.add(MultiLoader.CLASSPATH_SCHEME + packagePath(clazz));
        }
    }


    private void addStrict(CucumberOptions options, List<String> args) {
        if (options.strict()) {
            args.add("--strict");
        }
    }

    private void addJunitOptions(CucumberOptions options, List<String> args) {
        for (String junitOption : options.junit()) {
            args.add("--junit," + junitOption);
        }
    }

    static String packagePath(Class clazz) {
        return packagePath(packageName(clazz.getName()));
    }

    static String packagePath(String packageName) {
        return packageName.replace('.', '/');
    }

    static String packageName(String className) {
        return className.substring(0, Math.max(0, className.lastIndexOf(".")));
    }

    private boolean runningInEnvironmentWithoutAnsiSupport() {
        boolean intelliJidea = System.getProperty("idea.launcher.bin.path") != null;
        // TODO: What does Eclipse use?
        return intelliJidea;
    }

    private boolean hasSuperClass(Class classWithOptions) {
        return classWithOptions != Object.class;
    }

    private CucumberOptions getOptions(Class<?> clazz) {
        return clazz.getAnnotation(CucumberOptions.class);
    }
}
