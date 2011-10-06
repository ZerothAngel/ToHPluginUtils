# ToHPluginUtils - ZerothAngel's Bukkit plugin library #

**DO NOT USE &mdash; STILL UNSTABLE**

This is my personal library for [Bukkit](http://bukkit.org/) plugins. Heavily
inspired by elements of the [Spring Framework](http://www.springsource.org/)
(namely the annotation-driven web MVC framework and also somewhat the
TransactionTemplate), this library is largely concerned with command-line
parsing.

To give an idea of its usage, here's a snippet of the `promote` command
in [zPermissions](http://dev.bukkit.org/server-mods/zpermissions/):

```java
    @Command("promote")
    @Require("zpermissions.promote")
    public void promote(CommandSender sender, @Option("player") String playerName, @Option(value="track", optional=true) String trackName) {
        ...
    }
```

Some things to notice:

*   Permissions can be defined declaratively. Although only one permission is
    listed in the above example, more can be specified. You can also say whether
    all or just one permission is required.

*   Command arguments and switches are automatically parsed and passed as
    method arguments. The library will automatically recognize certain
    parameter types such as CommandSender, Server, or your Plugin type.
	Other types (annotated with @Option) receive values from the user.

*   Not evident above, but the library will also automatically generate the
     'usage' string for each command. For example:
	 
        /promote <player> [track]

    I decided to stick mainly with POSIX command-line conventions (e.g. all options
    must come before positional arguments). The order of the method's parameters
    play a role in determining the expected order of the positional arguments.

Other features:

*   Supports sub-commands to an arbitrary depth.

*   Commands may partially parse the command-line before handing off the rest
    to a sub-command. They may also pass arbitrary values to child sub-commands
    using a sort of backchannel (the CommandSession). CommandSession values
    can be automatically passed to a command method's parameter using the
    @Session annotation.

*   Automatic registration of top-level commands.

And some non-command-line-parsing features:

*   Programmatic permission checking which will throw an exception if the
    permission check fails. The library's CommandExecutor will automatically
    display a helpful error message.

*   Abstraction of transaction handling. Makes it easy to switch DAOs from say,
    Avaje Ebeans to JDBC to YAML.

*   A bunch of convenience methods. For example, a feature-rich version of
    CommandSender.sendMessage() which supports String.format() formatting
    and multiple lines.

*   A caching colorizing method which embeds color codes into strings. Supports
    verbose color codes using curly-braces, such as `{RED}This is red`. Also
    supports short, 2-character escape sequences (backtick + code). This helps
    avoid using string concatenation to build colored strings.

*   A wrapper around Logger.log() that supports String.format() formatting as
    well as logging throwables.

Generally, anything I find I need in two or more plugin projects is refactored
out into this library.

## Projects ##

This library is used in most of my Bukkit plugins:

*   zPermissions
*   Excursion
*   BoatControl
*   A few other unpublished plugins

## To Do ##

*   Perhaps add an option to make permission error messages more terse. (They
    currently display the required permissions.) Security conscious people might
    favor giving as little information to the user as possible, but I'm still
    not sure how beneficial this would be. A plugin's permission nodes should
    generally be well-documented, right? So what's the point of hiding them...

*   Configuration parsing seems a bit boilerplate. Maybe something can be
    abstracted out?

*   A JdbcTransactionStrategy. Would probably also mean the library needs to
    provide a method to get JDBC connections (that are already in a
    transaction). Not a fan of going down this road since that means I'd be
    writing SQL to specific databases (for anything moderately complex).
