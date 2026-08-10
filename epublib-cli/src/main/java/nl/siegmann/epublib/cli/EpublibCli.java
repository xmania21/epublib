package nl.siegmann.epublib.cli;

import nl.siegmann.epublib.cli.cmd.ConvertCommand;
import nl.siegmann.epublib.cli.cmd.ReadCommand;
import nl.siegmann.epublib.cli.cmd.ValidateCommand;
import nl.siegmann.epublib.cli.cmd.WriteCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Top-level PicoCLI entry point for the epublib command-line tool.
 *
 * <p>Usage: {@code epublib [COMMAND] [OPTIONS]}</p>
 *
 * <p>Subcommands:</p>
 * <ul>
 *   <li>{@code read}     — inspect/dump metadata from an EPUB</li>
 *   <li>{@code write}    — create an EPUB from a directory of HTML files</li>
 *   <li>{@code validate} — validate an EPUB against the specification</li>
 *   <li>{@code convert}  — convert an EPUB (e.g. re-pack, apply processors)</li>
 * </ul>
 *
 * @since 5.0
 */
@Command(
        name = "epublib",
        description = "epublib 5.0 — read, write, validate and convert EPUB files",
        mixinStandardHelpOptions = true,
        version = "epublib 5.0-SNAPSHOT",
        subcommands = {
                ReadCommand.class,
                WriteCommand.class,
                ValidateCommand.class,
                ConvertCommand.class,
                CommandLine.HelpCommand.class
        }
)
public class EpublibCli implements Runnable {

    /**
     * Called when no subcommand is specified; prints usage.
     */
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new EpublibCli()).execute(args);
        System.exit(exitCode);
    }
}
