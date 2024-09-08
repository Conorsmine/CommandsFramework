package io.github.jwdeveloper.commands.core;

import io.github.jwdeveloper.commands.api.services.ValidationService;
import io.github.jwdeveloper.commands.core.impl.parsers.BoolParser;
import io.github.jwdeveloper.commands.core.impl.parsers.NumberParser;
import io.github.jwdeveloper.commands.core.impl.parsers.TextParser;
import io.github.jwdeveloper.commands.core.impl.services.DefaultValidationService;
import io.github.jwdeveloper.dependance.Dependance;
import io.github.jwdeveloper.dependance.implementation.DependanceContainerBuilder;
import io.github.jwdeveloper.commands.api.Commands;
import io.github.jwdeveloper.commands.api.CommandsRegistry;
import io.github.jwdeveloper.commands.api.TemplateParser;
import io.github.jwdeveloper.commands.api.argumetns.ArgumentTypeBuilder;
import io.github.jwdeveloper.commands.api.argumetns.ArgumentsTypesRegistry;
import io.github.jwdeveloper.commands.api.builders.CommandBuilder;
import io.github.jwdeveloper.commands.api.data.DisplayAttribute;
import io.github.jwdeveloper.commands.api.patterns.PatternsRegistry;
import io.github.jwdeveloper.commands.api.services.ActionsRegistry;
import io.github.jwdeveloper.commands.api.services.MessagesService;
import io.github.jwdeveloper.commands.core.impl.DefaultArgumentTypesRegistry;
import io.github.jwdeveloper.commands.core.impl.DefaultCommandsRegistry;
import io.github.jwdeveloper.commands.core.impl.DefaultCommands;
import io.github.jwdeveloper.commands.core.impl.builders.ArgumentTypeBuilderImpl;
import io.github.jwdeveloper.commands.core.impl.builders.CommandBuilderImpl;
import io.github.jwdeveloper.commands.core.impl.patterns.PatternParser;
import io.github.jwdeveloper.commands.core.impl.patterns.PatternService;
import io.github.jwdeveloper.commands.core.impl.patterns.PatternsImpl;
import io.github.jwdeveloper.commands.core.impl.services.ActionBindingService;
import io.github.jwdeveloper.commands.core.impl.services.ActionsRegistryImpl;
import io.github.jwdeveloper.commands.core.impl.services.MessagesServiceImpl;
import io.github.jwdeveloper.commands.core.impl.templates.TemplateService;

import java.util.function.Consumer;

public class CommandFrameworkBuilder {

    public static Commands create(Consumer<DependanceContainerBuilder> onDependecines) {
        var containerBuilder = Dependance.newContainer();
        containerBuilder.registerSingleton(Commands.class, DefaultCommands.class);
        containerBuilder.registerSingleton(CommandsRegistry.class, DefaultCommandsRegistry.class);
        containerBuilder.registerSingleton(ActionsRegistry.class, ActionsRegistryImpl.class);
        containerBuilder.registerSingleton(PatternsRegistry.class, PatternsImpl.class);
        containerBuilder.registerSingleton(ArgumentsTypesRegistry.class, DefaultArgumentTypesRegistry.class);
        containerBuilder.registerSingleton(MessagesService.class, MessagesServiceImpl.class);
        containerBuilder.registerSingleton(ValidationService.class, DefaultValidationService.class);

        containerBuilder.registerTransient(ArgumentTypeBuilder.class, ArgumentTypeBuilderImpl.class);
        containerBuilder.registerTransient(CommandBuilder.class, CommandBuilderImpl.class);
        containerBuilder.registerTransient(TemplateParser.class, TemplateService.class);
        containerBuilder.registerSingleton(ActionBindingService.class);
        containerBuilder.registerTransient(PatternService.class);
        containerBuilder.registerSingleton(PatternParser.class);

        onDependecines.accept(containerBuilder);

        var container = containerBuilder.build();
        var commands = container.find(Commands.class);

        var argumentTypes = commands.argumentTypes();
        argumentTypes.register(new TextParser());
        argumentTypes.register(new NumberParser());
        argumentTypes.register(new BoolParser());

        var bindingsService = container.find(ActionBindingService.class);
        var patterns = commands.patterns();
        patterns.mapProperty("dn", (value, argBuilder, s) -> argBuilder.withDisplayName());
        patterns.mapProperty("dt", (value, argBuilder, s) -> argBuilder.withDisplayType());
        patterns.mapProperty("dd", (value, argBuilder, s) -> argBuilder.withDisplayDescription());
        patterns.mapProperty("de", (value, argBuilder, s) -> argBuilder.withDisplayError());
        patterns.mapProperty("ds", (value, argBuilder, s) -> argBuilder.withDisplaySuggestions());
        patterns.mapProperty("d-", (value, argBuilder, s) -> argBuilder.withDisplayNone());
        patterns.mapProperty("da", (value, argBuilder, s) -> argBuilder.withDisplayAttribute(DisplayAttribute.values()));
        patterns.mapProperty("d", (value, argBuilder, s) -> argBuilder.withDescription(value));
        patterns.mapProperty("p", (value, argBuilder, source) ->
        {
            var parserAction = bindingsService.bindParseMethod(source, value);
            argBuilder.withParser(parserAction);
        });
        patterns.mapProperty("s", (value, argBuilder, source) ->
        {
            var suggestionAction = bindingsService.bindSuggestionsMethod(source, value);
            argBuilder.withSuggestions(suggestionAction);
        });

        return commands;
    }

}