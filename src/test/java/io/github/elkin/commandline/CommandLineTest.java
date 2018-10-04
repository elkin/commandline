package io.github.elkin.commandline;

import io.github.elkin.commandline.exception.CheckException;
import io.github.elkin.commandline.exception.DuplicateNameException;
import io.github.elkin.commandline.exception.DuplicatePrefixException;
import io.github.elkin.commandline.exception.NoValueException;
import io.github.elkin.commandline.exception.SizeViolationException;
import io.github.elkin.commandline.exception.UnhandledArgumentException;
import io.github.elkin.commandline.exception.UnknownNameException;
import io.github.elkin.commandline.exception.UnknownPrefixException;
import io.github.elkin.commandline.exception.ValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CommandLineTest {
    private CommandLineConfiguration configuration;

    private CommandLine getCommandLine(CommandLineConfiguration configuration, String[] args)
    {
        return CommandLine.getCommandLine(configuration, args, Util.reThrowExceptionHandler());
    }

    private void parse(CommandLineConfiguration configuration, String[] args)
    {
        CommandLine.parse(configuration, args, Util.reThrowExceptionHandler());
    }

    @BeforeMethod
    public void setup()
    {
        configuration = new CommandLineConfiguration();
    }

    @Test
    public void noArguments()
    {
        OptionalArgument argument = configuration.addOptionalArgument("address");
        CommandLine commandLine = getCommandLine(configuration, new String[0]);
        assertTrue(commandLine.get("address").isEmpty());
        assertTrue(!argument.value().isPresent());
    }

    @Test
    public void noOptions()
    {
        Option option = configuration.addOption("address", "-a");
        CommandLine commandLine = getCommandLine(configuration, new String[0]);
        assertTrue(commandLine.get("address").isEmpty());
        assertTrue(option.values().isEmpty());
    }

    @Test
    public void noFlags()
    {
        Flag flag = configuration.addFlag("flag", "-a");
        CommandLine commandLine = getCommandLine(configuration, new String[0]);
        assertFalse(commandLine.isFlagSet("flag"));

        assertFalse(flag.isSet());
    }

    @Test
    public void oneArgument()
    {
        OptionalArgument argument = configuration.addOptionalArgument("port");
        String port = "1000";
        CommandLine commandLine = getCommandLine(configuration, new String[]{port});
        assertEquals(commandLine.get("port").size(), 1);
        assertEquals(commandLine.get("port").getFirstValue(), port);

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "1000");
    }

    @Test
    public void oneOption()
    {
        Option option = configuration.addOption("test", "--test");
        String[] args = { "--test", "1st" };
        CommandLine commandLine = getCommandLine(configuration, args);
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "1st");
    }

    @Test
    public void oneFlag()
    {
        Flag flag = configuration.addFlag("flag", "-f");
        CommandLine commandLine = getCommandLine(configuration, new String[] {"-f"});
        assertTrue(commandLine.isFlagSet("flag"));

        assertTrue(flag.isSet());
    }

    @Test
    public void oneArgumentWithConsumer()
    {
        AtomicInteger counter = new AtomicInteger();
        OptionalArgument arg = configuration.addOptionalArgument("number");
        arg.setConsumer(argument -> counter.set(Integer.parseInt(argument)));

        CommandLine commandLine = getCommandLine(configuration, new String[] { "3" });
        assertEquals(commandLine.get("number").size(), 1);
        assertEquals(commandLine.get("number").getFirstValue(), "3");
        assertEquals(counter.get(), 3);

        assertTrue(arg.value().isPresent());
        assertEquals(arg.value().get(), "3");
    }

    @Test
    public void oneArgumentWithChecker()
    {
        OptionalArgument argument = configuration.addOptionalArgument("ticker");
        argument.setChecker(value -> value.length() <= 3);

        CommandLine commandLine = getCommandLine(configuration, new String[] { "IBM" });
        assertEquals(commandLine.get("ticker").size(), 1);
        assertEquals(commandLine.get("ticker").getFirstValue(), "IBM");

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "IBM");
    }

    @Test(expectedExceptions = CheckException.class)
    public void oneArgumentWithCheckerFail()
    {
        configuration.addOptionalArgument("ticker")
                .setChecker(value -> value.length() > 3);

        getCommandLine(configuration, new String[] { "IBM" });
    }

    @Test(expectedExceptions = CheckException.class)
    public void oneArgumentWithCheckerFailParseCommandLine()
    {
        configuration.addOptionalArgument("ticker")
                .setChecker(value -> value.length() > 3);

        parse(configuration, new String[] { "IBM" });
    }

    @Test
    public void oneArgumentOneIsRequired()
    {
        RequiredArgument argument = configuration.addRequiredArgument("port");
        String port = "1000";
        CommandLine commandLine = getCommandLine(configuration, new String[]{port});
        assertEquals(commandLine.get("port").size(), 1);
        assertEquals(commandLine.get("port").getFirstValue(), port);

        assertEquals(argument.value(), "1000");
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void noArgumentsOneIsRequired()
    {
        configuration.addRequiredArgument("port");
        getCommandLine(configuration, new String[0]);
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void noArgumentsOneIsRequiredParseCommandLine()
    {
        configuration.addRequiredArgument("port");
        parse(configuration, new String[0]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void zeroIsMaxNumberOfValuesForLastArgument()
    {
        configuration.setMaxLastArgumentSize(0);
    }

    @Test
    public void noArgumentsMaxIsOne()
    {
        OptionalArgument argument = configuration.addOptionalArgument("test");

        CommandLine commandLine = getCommandLine(configuration, new String[0]);
        assertTrue(commandLine.get("test").isEmpty());

        assertFalse(argument.value().isPresent());
    }

    @Test
    public void oneArgumentMaxIsOne()
    {
        OptionalArgument argument = configuration.addOptionalArgument("test");

        CommandLine commandLine = getCommandLine(configuration, new String[] { "1st" });
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "1st");
    }

    @Test
    public void twoArguments()
    {
        OptionalArgument argument1 = configuration.addOptionalArgument("test");
        OptionalArgument argument2 = configuration.addOptionalArgument("test2");

        CommandLine commandLine = getCommandLine(configuration, new String[]{"1st", "2nd"});
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");
        assertEquals(commandLine.get("test2").size(), 1);
        assertEquals(commandLine.get("test2").getFirstValue(), "2nd");

        assertTrue(argument1.value().isPresent());
        assertEquals(argument1.value().get(), "1st");
        assertTrue(argument2.value().isPresent());
        assertEquals(argument2.value().get(), "2nd");
    }

    @Test
    public void twoArgumentsOneGiven()
    {
        OptionalArgument argument1 = configuration.addOptionalArgument("test");
        OptionalArgument argument2 = configuration.addOptionalArgument("test2");

        CommandLine commandLine = getCommandLine(configuration, new String[]{"1st"});
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");
        assertEquals(commandLine.get("test2").size(), 0);

        assertTrue(argument1.value().isPresent());
        assertEquals(argument1.value().get(), "1st");
        assertFalse(argument2.value().isPresent());
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void twoArgumentsMaxIsOne()
    {
        configuration.addOptionalArgument("test");
        configuration.setMaxLastArgumentSize(1);

        getCommandLine(configuration, new String[] { "1st", "2nd" });
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void twoArgumentsMaxIsOneParseCommandLine()
    {
        configuration.addOptionalArgument("test");
        configuration.setMaxLastArgumentSize(1);

        parse(configuration, new String[] { "1st", "2nd" });
    }

    @Test
    public void twoArgumentsMinIsOneMaxIsThree()
    {
        RequiredArgument argument = configuration.addRequiredArgument("test");
        configuration.setMaxLastArgumentSize(3);

        CommandLine commandLine = getCommandLine(configuration, new String[] {"1", "2"});
        assertEquals(commandLine.get("test").size(), 2);
        assertEquals(commandLine.get("test").getValue(0), "1");
        assertEquals(commandLine.get("test").getValue(1), "2");

        assertEquals(argument.value(), "1");
        Values values = argument.remainder();
        assertEquals(values.size(), 1);
        assertEquals(values.getFirstValue(), "2");
    }

    @Test
    public void twoArgumentsMaxIsTwo()
    {
        RequiredArgument argument = configuration.addRequiredArgument("test");
        configuration.setMaxLastArgumentSize(2);

        CommandLine commandLine = getCommandLine(configuration, new String[] {"1", "2"});
        assertEquals(commandLine.get("test").size(), 2);
        assertEquals(commandLine.get("test").getValue(0), "1");
        assertEquals(commandLine.get("test").getValue(1), "2");

        assertEquals(argument.value(), "1");

        Values values = argument.remainder();
        assertEquals(values.size(), 1);
        assertEquals(values.getValue(0), "2");
    }

    @Test
    public void oneArgumentWithOneDefaultValue()
    {
        OptionalArgument argument = configuration.addOptionalArgument("test");
        argument.addDefaultValue("1st");

        CommandLine commandLine = getCommandLine(configuration, new String[0]);
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "1st");
    }

    @Test
    public void oneArgumentWithTwoDefaultValues()
    {
        OptionalArgument argument = configuration.addOptionalArgument("test");
        argument.addDefaultValue("1st")
                .addDefaultValue("2nd");
        configuration.setMaxLastArgumentSize(2);

        CommandLine commandLine = getCommandLine(configuration, new String[0]);
        assertEquals(commandLine.get("test").size(), 2);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");
        assertEquals(commandLine.get("test").getValue(1), "2nd");

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "1st");

        Values remainder = argument.remainder();
        assertEquals(remainder.size(), 1);
        assertEquals(remainder.getValue(0), "2nd");
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void oneArgumentTwoDefaultValueMaxIsOne()
    {
        configuration.addOptionalArgument("test")
                .addDefaultValue("1st")
                .addDefaultValue("2nd");
        configuration.setMaxLastArgumentSize(1);

        getCommandLine(configuration, new String[0]);
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void oneArgumentTwoDefaultValueMaxIsOneParseCommandLine()
    {
        configuration.addOptionalArgument("test")
                .addDefaultValue("1st")
                .addDefaultValue("2nd");
        configuration.setMaxLastArgumentSize(1);

        parse(configuration, new String[0]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void argumentAddNullDefaultValue()
    {
        configuration.addOptionalArgument("test")
                .addDefaultValue(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void argumentAddEmptyDefaultValue()
    {
        configuration.addOptionalArgument("test")
                .addDefaultValue("");
    }

    @Test
    public void oneArgumentWithDefaultValueProvided()
    {
        OptionalArgument argument = configuration.addOptionalArgument("test");
        argument.addDefaultValue("1st");

        CommandLine commandLine = getCommandLine(configuration, new String[] {"arg"});
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "arg");

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "arg");
    }

    @Test
    public void oneOptionWithConsumer()
    {
        AtomicInteger counter = new AtomicInteger();
        Option option = configuration.addOption("number", "-n")
                .setConsumer(argument -> counter.set(Integer.parseInt(argument)));

        CommandLine commandLine = getCommandLine(configuration, new String[] { "-n", "3" });
        assertEquals(commandLine.get("number").size(), 1);
        assertEquals(commandLine.get("number").getFirstValue(), "3");
        assertEquals(counter.get(), 3);

        Values values = option.values();
        assertEquals(values.size(), 1);
        assertEquals(values.getFirstValue(), "3");
    }

    @Test
    public void oneOptionWithChecker()
    {
        Option option = configuration.addOption("ticker", "--ticker")
                .setChecker(value -> value.length() <= 3);

        CommandLine commandLine = getCommandLine(configuration, new String[] { "--ticker", "IBM" });
        assertEquals(commandLine.get("ticker").size(), 1);
        assertEquals(commandLine.get("ticker").getFirstValue(), "IBM");

        Values values = option.values();
        assertEquals(values.size(), 1);
        assertEquals(values.getFirstValue(), "IBM");
    }

    @Test(expectedExceptions = CheckException.class)
    public void oneOptionWithCheckerFail()
    {
        configuration.addOption("ticker", "-t")
                .setChecker(value -> value.length() > 3);

        getCommandLine(configuration, new String[] { "-t", "IBM" });
    }

    @Test(expectedExceptions = CheckException.class)
    public void oneOptionWithCheckerFailParseCommandLine()
    {
        configuration.addOption("ticker", "-t")
                .setChecker(value -> value.length() > 3);

        parse(configuration, new String[] { "-t", "IBM" });
    }

    @Test
    public void oneOptionChoice()
    {
        Option option = configuration.addOption("option", "--opt")
                .setChecker(Util.choice("1", "2", "a"));

        parse(configuration, new String[] {"--opt=1"});
        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "1");
    }

    @Test(expectedExceptions = CheckException.class)
    public void oneOptionChoiceFailure()
    {
        Option option = configuration.addOption("option", "--opt")
                .setChecker(Util.choice("1", "2", "a"));

        parse(configuration, new String[] {"--opt=b"});
    }

    @Test
    public void oneOptionOneIsRequired()
    {
        Option option = configuration.addOption("port", "--port")
                .require();

        CommandLine commandLine = getCommandLine(configuration, new String[]{"--port", "1000"});
        assertEquals(commandLine.get("port").size(), 1);
        assertEquals(commandLine.get("port").getFirstValue(), "1000");

        Values values = option.values();
        assertEquals(values.size(), 1);
        assertEquals(values.getFirstValue(), "1000");
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void noOptionsOneIsRequired()
    {
        configuration.addOption("port", "-p")
                .addPrefix("--port")
                .require();
        getCommandLine(configuration, new String[0]);
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void noOptionsOneIsRequiredParseCommandLine()
    {
        configuration.addOption("port", "-p")
                .addPrefix("--port")
                .require();
        parse(configuration, new String[0]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void zeroIsMaxOptions()
    {
        configuration.addOption("test", "-t").setMaxNumberOfValues(0);
        getCommandLine(configuration, new String[0]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void zeroIsMaxOptionsParseCommandLine()
    {
        configuration.addOption("test", "-t").setMaxNumberOfValues(0);
        parse(configuration, new String[0]);
    }

    @Test
    public void noOptionsMaxIsTwo()
    {
        Option option = configuration.addOption("test", "--test")
                .addPrefix("-t")
                .setMaxNumberOfValues(2);

        CommandLine commandLine = getCommandLine(configuration, new String[0]);
        assertTrue(commandLine.get("test").isEmpty());

        assertTrue(option.values().isEmpty());
    }

    @Test
    public void oneOptionMaxIsOne()
    {
        Option option = configuration.addOption("test", "-t")
                .addPrefix("--test")
                .setMaxNumberOfValues(1);

        CommandLine commandLine = getCommandLine(configuration, new String[] { "-t", "1st" });
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");

        Values values = option.values();
        assertEquals(values.size(), 1);
        assertEquals(values.getFirstValue(), "1st");
    }

    @Test
    public void twoOptions()
    {
        Option option1 = configuration.addOption("test", "-t");
        Option option2 = configuration.addOption("test2", "--test2");

        CommandLine commandLine = getCommandLine(
            configuration, new String[]{"--test2", "1st", "-t", "2nd"});
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "2nd");
        assertEquals(commandLine.get("test2").size(), 1);
        assertEquals(commandLine.get("test2").getFirstValue(), "1st");

        assertEquals(option1.values().size(), 1);
        assertEquals(option1.values().getFirstValue(), "2nd");

        assertEquals(option2.values().size(), 1);
        assertEquals(option2.values().getFirstValue(), "1st");
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void twoOptionsMaxIsOne()
    {
        configuration.addOption("test", "-t")
                .addPrefix("--test")
                .setMaxNumberOfValues(1);

        getCommandLine(configuration, new String[] { "-t", "1st", "--test", "2nd" });
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void twoOptionsMaxIsOneParseCommandLine()
    {
        configuration.addOption("test", "-t")
                .addPrefix("--test")
                .setMaxNumberOfValues(1);

        parse(configuration, new String[] { "-t", "1st", "--test", "2nd" });
    }

    @Test
    public void twoValuesOptionMinIsOneMaxIsThree()
    {
        Option option = configuration.addOption("test", "--test")
                .addPrefix("-t")
                .require()
                .setMaxNumberOfValues(3);

        CommandLine commandLine = getCommandLine(
            configuration, new String[] {"--test", "1", "-t", "2"});
        assertEquals(commandLine.get("test").size(), 2);
        assertEquals(commandLine.get("test").getValue(0), "1");
        assertEquals(commandLine.get("test").getValue(1), "2");

        Values values = option.values();
        assertEquals(values.size(), 2);
        assertEquals(values.getFirstValue(), "1");
        assertEquals(values.getValue(1), "2");
    }

    @Test
    public void twoValuesOptionMinIsTwoMaxIsTwo()
    {
        Option option = configuration.addOption("test", "--test")
                .addPrefix("-t")
                .setMaxNumberOfValues(2)
                .require();

        CommandLine commandLine = getCommandLine(
            configuration, new String[] {"-t", "1", "--test", "2"});
        assertEquals(commandLine.get("test").size(), 2);
        assertEquals(commandLine.get("test").getValue(0), "1");
        assertEquals(commandLine.get("test").getValue(1), "2");

        Values optionValues = option.values();
        assertEquals(optionValues.size(), 2);
        assertEquals(optionValues.getValue(0), "1");
        assertEquals(optionValues.getValue(1), "2");
    }

    @Test
    public void oneOptionOneDefaultValue()
    {
        Option option = configuration.addOption("test", "--test")
                .addDefaultValue("1st");

        CommandLine commandLine = getCommandLine(configuration, new String[0]);
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "1st");
    }

    @Test
    public void oneOptionTwoDefaultValues()
    {
        Option option = configuration.addOption("test", "--test")
                .setMaxNumberOfValues(2)
                .addDefaultValue("1st")
                .addDefaultValue("2nd");

        CommandLine commandLine = getCommandLine(configuration, new String[0]);
        assertEquals(commandLine.get("test").size(), 2);
        assertEquals(commandLine.get("test").getFirstValue(), "1st");
        assertEquals(commandLine.get("test").getValue(1), "2nd");

        Values optionValues = option.values();
        assertEquals(optionValues.size(), 2);
        assertEquals(optionValues.getFirstValue(), "1st");
        assertEquals(optionValues.getValue(1), "2nd");
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void oneOptionMaxIsOneTwoDefaultValues()
    {
        configuration.addOption("test", "--test")
                .setMaxNumberOfValues(1)
                .addDefaultValue("1st")
                .addDefaultValue("2nd");

        getCommandLine(configuration, new String[0]);
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void oneOptionMaxIsOneTwoDefaultValuesParseCommandLine()
    {
        configuration.addOption("test", "--test")
                .setMaxNumberOfValues(1)
                .addDefaultValue("1st")
                .addDefaultValue("2nd");

        parse(configuration, new String[0]);
    }

    @Test
    public void twoAvailableFlagsNoProvided()
    {
        Flag flag1 = configuration.addFlag("test", "-t")
                .addPrefix("--test");
        Flag flag2 = configuration.addFlag("test2", "--second-test");

        CommandLine commandLine = getCommandLine(configuration, new String[0]);
        assertFalse(commandLine.isFlagSet("test"));
        assertFalse(commandLine.isFlagSet("test2"));

        assertFalse(flag1.isSet());
        assertFalse(flag2.isSet());
    }

    @Test
    public void twoFlags()
    {
        Flag flag1 = configuration.addFlag("test", "-t")
                .addPrefix("--test")
                .setMaxNumberOfValues(2);
        Flag flag2 = configuration.addFlag("test2", "--second-test");

        CommandLine commandLine = getCommandLine(
            configuration, new String[] {"-t", "--second-test", "--test"});
        assertTrue(commandLine.isFlagSet("test"));
        assertTrue(commandLine.isFlagSet("test2"));

        assertTrue(flag1.isSet());
        assertTrue(flag2.isSet());
    }

    @Test
    public void twoFlagsOneProvided()
    {
        Flag flag1 = configuration.addFlag("test", "-t")
                .addPrefix("--test");
        Flag flag2 = configuration.addFlag("test2", "--second-test");

        CommandLine commandLine = getCommandLine(configuration, new String[] {"--second-test"});
        assertFalse(commandLine.isFlagSet("test"));
        assertTrue(commandLine.isFlagSet("test2"));

        assertFalse(flag1.isSet());
        assertTrue(flag2.isSet());
    }

    @Test
    public void flagIsRequiredAndProvided()
    {
        Flag flag = configuration.addFlag("flag", "--flag").require();

        CommandLine commandLine = getCommandLine(configuration, new String[] { "--flag" });
        assertTrue(commandLine.isFlagSet("flag"));

        assertTrue(flag.isSet());
    }

    @Test
    public void flagIsRequiredAndProvidedParseCommandLine()
    {
        Flag flag = configuration.addFlag("flag", "--flag").require();

        parse(configuration, new String[] { "--flag" });

        assertTrue(flag.isSet());
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void flagIsRequired()
    {
        configuration.addFlag("flag", "--flag").require();

        getCommandLine(configuration, new String[0]);
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void flagIsRequiredParseCommandLine()
    {
        configuration.addFlag("flag", "--flag").require();

        parse(configuration, new String[0]);
    }

    @Test(expectedExceptions = UnknownPrefixException.class)
    public void unknownOptionPrefix()
    {
        configuration.addOption("test", "-t");
        getCommandLine(configuration, new String[] {"-s", "v"});
    }

    @Test(expectedExceptions = UnknownPrefixException.class)
    public void unknownOptionPrefixParseCommandLine()
    {
        configuration.addOption("test", "-t");
        parse(configuration, new String[] {"-s", "v"});
    }

    @Test(expectedExceptions = UnknownPrefixException.class)
    public void unknownFlagPrefix()
    {
        configuration.addFlag("test", "-t");
        getCommandLine(configuration, new String[] {"-s"});
    }

    @Test(expectedExceptions = UnknownPrefixException.class)
    public void unknownFlagPrefixParseCommandLine()
    {
        configuration.addFlag("test", "-t");
        parse(configuration, new String[] {"-s"});
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void doubleArgumentName()
    {
        configuration.addOptionalArgument("arg");
        configuration.addOptionalArgument("arg");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void doubleOptionName()
    {
        configuration.addOption("arg", "-a");
        configuration.addOption("arg", "-t");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void optionNameIsEqualToArgument()
    {
        configuration.addOptionalArgument("arg");
        configuration.addOption("arg", "-a");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void argumentNameIsEqualToArgument()
    {
        configuration.addOption("test", "-t");
        configuration.addOptionalArgument("test");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void argumentNameIsEqualToFlag()
    {
        configuration.addFlag("flag", "--flag");
        configuration.addOptionalArgument("flag");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void flagNameIsEqualToArgument()
    {
        configuration.addOptionalArgument("arg");
        configuration.addFlag("arg", "-a");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void flagNameIsEqualToOption()
    {
        configuration.addOption("opt", "--option");
        configuration.addFlag("opt", "-o");
    }

    @Test(expectedExceptions = DuplicateNameException.class)
    public void optionNameIsEqualToFlag()
    {
        configuration.addFlag("flag", "-f");
        configuration.addOption("flag", "--flag");
    }

    @Test(expectedExceptions = NoValueException.class)
    public void optionDoesntHaveValue()
    {
        configuration.addOption("test", "--test");

        getCommandLine(configuration, new String[]{"--test"});
    }

    @Test(expectedExceptions = NoValueException.class)
    public void optionDoesntHaveValueParseCommandLine()
    {
        configuration.addOption("test", "--test");

        parse(configuration, new String[]{"--test"});
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void doublePrefixTwoOptions()
    {
        configuration.addOption("test", "-t");
        configuration.addOption("test2", "-t");

        getCommandLine(configuration, new String[0]);
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void doublePrefixTwoOptionsParseCommandLine()
    {
        configuration.addOption("test", "-t");
        configuration.addOption("test2", "-t");

        parse(configuration, new String[0]);
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void doublePrefixTwoFlags()
    {
        configuration.addFlag("test", "-t");
        configuration.addFlag("test2", "-t");

        getCommandLine(configuration, new String[0]);
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void doublePrefixTwoFlagsParseCommandLine()
    {
        configuration.addFlag("test", "-t");
        configuration.addFlag("test2", "-t");

        parse(configuration, new String[0]);
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void doublePrefixOptionFlag()
    {
        configuration.addFlag("test", "-t");
        configuration.addOption("test2", "-t");

        getCommandLine(configuration, new String[0]);
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void doublePrefixOptionFlagParseCommandLine()
    {
        configuration.addFlag("test", "-t");
        configuration.addOption("test2", "-t");

        parse(configuration, new String[0]);
    }

    @Test(expectedExceptions = UnknownNameException.class)
    public void unknownName()
    {
        CommandLine commandLine = getCommandLine(configuration, new String[0]);
        commandLine.get("test");
    }

    @Test(expectedExceptions = UnknownNameException.class)
    public void unknownNameOption()
    {
        configuration.addOption("test", "--test");
        CommandLine commandLine = getCommandLine(configuration, new String[]{"--test", "abc"});
        assertEquals(commandLine.get("test").size(), 1);
        commandLine.get("test2");
    }

    @Test(expectedExceptions = UnknownNameException.class)
    public void unknownNameFlag()
    {
        configuration.addFlag("skip", "-s");
        CommandLine commandLine = getCommandLine(configuration, new String[] {"-s"});
        assertTrue(commandLine.isFlagSet("skip"));
        commandLine.isFlagSet("skip2");
    }

    @Test(expectedExceptions = UnknownNameException.class)
    public void unknownNameOptionFlag()
    {
        configuration.addFlag("skip", "-s");
        CommandLine commandLine = getCommandLine(configuration, new String[] {"-s"});
        commandLine.get("skip");
    }

    @Test
    public void defaultValueOptionIsProvided()
    {
        Option option = configuration.addOption("test", "-t")
                .addDefaultValue("1st");

        CommandLine commandLine = getCommandLine(configuration, new String[]{"-t", "2nd"});
        assertEquals(commandLine.get("test").size(), 1);
        assertEquals(commandLine.get("test").getFirstValue(), "2nd");

        Values optionValues = option.values();
        assertEquals(optionValues.size(), 1);
        assertEquals(optionValues.getValue(0), "2nd");
    }

    @Test(expectedExceptions = UnhandledArgumentException.class)
    public void unhandledArgument()
    {
        configuration.addOption("test", "--test");
        getCommandLine(configuration, new String[] {"arg"});
    }

    @Test(expectedExceptions = UnhandledArgumentException.class)
    public void unhandledArgumentParseCommandLine()
    {
        configuration.addOption("test", "--test");
        parse(configuration, new String[] {"arg"});
    }

    @Test
    public void optionValue()
    {
        Option option = configuration.addOption("config", "--config");
        parse(configuration, new String[] {"--config", "config.xml"});

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "config.xml");
    }

    @Test
    public void argumentValue()
    {
        OptionalArgument argument = configuration.addOptionalArgument("config");

        parse(configuration, new String[] {"config.xml"});

        assertTrue(argument.value().isPresent());
        assertEquals(argument.value().get(), "config.xml");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void argumentNullChecker()
    {
        configuration.addOptionalArgument("name")
                .setChecker(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void argumentNullConsumer()
    {
        configuration.addOptionalArgument("test")
                .setConsumer(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void argumentNullDescription()
    {
        configuration.addOptionalArgument("argument")
                .setDescription(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void optionNullChecker()
    {
        configuration.addOption("option", "-o")
                .setChecker(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void optionNullConsumer()
    {
        configuration.addOption("option", "-o")
                .setConsumer(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void optionNullDescription()
    {
        configuration.addOption("option", "-o")
                .setDescription(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void argumentNullName()
    {
        configuration.addOptionalArgument(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void argumentEmptyName()
    {
        configuration.addOptionalArgument("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionNullName()
    {
        configuration.addOption(null, "-o");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionEmptyName()
    {
        configuration.addOption("", "-o");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionNullPrefix()
    {
        configuration.addOption("option", null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionWrongPrefix()
    {
        configuration.addOption("option", "opt");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionEmptyPrefix()
    {
        configuration.addOption("option", "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionAddNullPrefix()
    {
        configuration.addOption("option", "--opt")
                .addPrefix(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionAddEmptyPrefix()
    {
        configuration.addOption("option", "-o")
                .addPrefix("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionAddWrongPrefix()
    {
        configuration.addOption("worker", "-w")
                .addPrefix("nw");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionAddNullDefaultValue()
    {
        configuration.addOption("option", "--opt")
                .addDefaultValue(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionAddEmptyDefaultValue()
    {
        configuration.addOption("option", "--opt")
                .addDefaultValue("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagNullName()
    {
        configuration.addFlag(null, "-f");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagEmptyName()
    {
        configuration.addFlag("", "-flag");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagNullPrefix()
    {
        configuration.addFlag("flag", null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagWrongPrefix()
    {
        configuration.addFlag("flag", "df");
    }

    @Test
    public void flagWithConsumer()
    {
        AtomicInteger counter = new AtomicInteger(0);
        Flag flag = configuration.addFlag("flag", "-f")
                .setConsumer(f -> counter.incrementAndGet())
                .setMaxNumberOfValues(2);

        CommandLine commandLine = getCommandLine(configuration, new String[]{"-ff"});
        assertTrue(commandLine.isFlagSet("flag"));

        assertTrue(flag.isSet());
        assertEquals(flag.numberOfFlags(), 2);
        assertEquals(counter.get(), 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagEmptyPrefix()
    {
        configuration.addFlag("flag", "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagAddNullPrefix()
    {
        configuration.addFlag("flag", "-f")
                .addPrefix(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagAddEmptyPrefix()
    {
        configuration.addFlag("flag", "--flag")
                .addPrefix("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void flagAddWrongPrefix()
    {
        configuration.addFlag("virtual", "-v")
                .addPrefix("v");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flagNullDescription()
    {
        configuration.addFlag("flag", "-f")
                .setDescription(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void configurationNullHelpGenerator()
    {
        new CommandLineConfiguration(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void configurationSetNullDescription()
    {
        configuration.setDescription(null);
    }

    @Test
    public void shortOption()
    {
        Option option = configuration.addOption("option", "-o");

        CommandLine commandLine = getCommandLine(configuration, new String[] { "-oa"});
        assertEquals(commandLine.get("option").size(), 1);
        assertEquals(commandLine.get("option").getFirstValue(), "a");

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "a");
    }

    @Test
    public void shortOptionParseCommandLine()
    {
        Option option = configuration.addOption("option", "-o");

        parse(configuration, new String[] { "-oa"});

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "a");
    }

    @Test
    public void longOptionWithEqualSign()
    {
        Option option = configuration.addOption("option", "--option");

        CommandLine commandLine = getCommandLine(configuration, new String[] {"--option=value"});
        assertEquals(commandLine.get("option").size(), 1);
        assertEquals(commandLine.get("option").getFirstValue(), "value");

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "value");
    }

    @Test
    public void longOptionWithEqualSignParseCommandLine()
    {
        Option option = configuration.addOption("option", "--option");

        parse(configuration, new String[] {"--option=value"});

        assertEquals(option.values().size(), 1);
        assertEquals(option.values().getFirstValue(), "value");
    }

    @Test
    public void flagsAreSticked()
    {
        Flag flag = configuration.addFlag("flag", "-f");
        Flag flag2 = configuration.addFlag("flag2", "-t");

        parse(configuration, new String[] {"-tf"});

        assertTrue(flag.isSet());
        assertTrue(flag2.isSet());
    }

    @Test
    public void flagWithFewValues()
    {
        Flag flag = configuration.addFlag("flag", "--flag")
                .addPrefix("-f")
                .addPrefix("-s")
                .setMaxNumberOfValues(3);

        CommandLine commandLine = getCommandLine(configuration, new String[] {"-sf", "--flag"});
        assertTrue(commandLine.isFlagSet("flag"));
        assertTrue(flag.isSet());
        assertEquals(flag.numberOfFlags(), 3);
    }

    @Test
    public void flagWithFewValuesParseCommandLine()
    {
        Flag flag = configuration.addFlag("flag", "--flag")
                .addPrefix("-f")
                .addPrefix("-s")
                .setMaxNumberOfValues(3);

        parse(configuration, new String[] {"-sf", "--flag"});
        assertTrue(flag.isSet());
        assertEquals(flag.numberOfFlags(), 3);
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void flagMaxNumberOfValuesExceeded()
    {
        configuration.addFlag("flag", "-t")
                .addPrefix("-f")
                .setMaxNumberOfValues(1);

        getCommandLine(configuration, new String[] {"-ft"});
    }

    @Test(expectedExceptions = SizeViolationException.class)
    public void flagMaxNumberOfValuesExceededParseCommandLine()
    {
        configuration.addFlag("flag", "-t")
                .addPrefix("-f")
                .setMaxNumberOfValues(1);

        parse(configuration, new String[] {"-ft"});
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void prefixOverlappingTwoOptions()
    {
        configuration.addOption("option", "--option");
        configuration.addOption("option2", "--opt");
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void prefixOverlappingTwoFlags()
    {
        configuration.addFlag("flag", "--flag");
        configuration.addFlag("flag2", "--fl");
    }

    @Test(expectedExceptions = DuplicatePrefixException.class)
    public void prefixOverlappingOptionFlag()
    {
        configuration.addFlag("flag", "--opt");
        configuration.addOption("option", "--option");
    }

    @Test(expectedExceptions = ValidationException.class)
    public void requiredArgumentAfterOptionArgument()
    {
        configuration.addOptionalArgument("test");
        configuration.addRequiredArgument("test2");
    }

    @Test(expectedExceptions = ValidationException.class)
    public void incompatibleOptions()
    {
        Option option = configuration.addOption("option", "-o");
        Option option2 = configuration.addOption("option2", "-t");

        GroupValidator checker = new GroupValidator();
        checker.addGroup()
                .addOption(option);
        checker.addGroup()
                .addOption(option2);
        configuration.addValidator(checker);

        parse(configuration, new String[] {"-oasd", "-tdf"});
    }

    @Test(expectedExceptions = ValidationException.class)
    public void incompatibleFlags()
    {
        Flag flag = configuration.addFlag("flag", "-s");
        Flag flag2 = configuration.addFlag("flag2", "-f");

        GroupValidator checker = new GroupValidator();
        checker.addGroup()
                .addFlag(flag);
        checker.addGroup()
                .addFlag(flag2);
        configuration.addValidator(checker);

        parse(configuration, new String[] {"-fs"});
    }

    @Test(expectedExceptions = ValidationException.class)
    public void incompatibleOptionFlag()
    {
        Flag flag = configuration.addFlag("flag", "-s");
        Option option = configuration.addOption("option", "--option");

        GroupValidator checker = new GroupValidator();
        checker.addGroup()
                .addFlag(flag);
        checker.addGroup()
                .addOption(option);
        configuration.addValidator(checker);

        parse(configuration, new String[] {"-s", "--option=1"});
    }

    @Test
    public void defaultCommandLineConfigurationDescriptionIsEmptyString()
    {
        CommandLineConfiguration configuration = new CommandLineConfiguration();
        assertEquals(configuration.description(), "");
    }

}
