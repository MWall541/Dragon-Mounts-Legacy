# Addon Example
___
This is a complete and working example of an addon for DM:L. It implements a completely new Dragon Breed with ZERO code,
little bit of json work, and file management, is all it takes.

DM:L uses a combination of both data and assets in order to achieve creating new breeds. Data describes the breed's
characteristics and logic (such as colors or habitats) and the assets describe the textures and models (such as bodies,
saddles, and dragon eggs.)

While it is possible to achieve this by using both a datapack and resourcepack, it's quite inconvenient when making
new worlds, distributing it to players, etc. Which is why I highly recommend making your addon a **Forge "lowcode" Mod**.

A "lowcode" mod is basically what it says on the tin. lowcode. Or no code at all, more likely. To Achieve this, your
files need to be archived as a jar, which is pretty trivial if you've made it this far anyway. **Just package your files
inside a zip and rename the file extension to `.jar`**. Yes, it's safe and it does work, and yes, I realize its not
ideal, but it's better than before.