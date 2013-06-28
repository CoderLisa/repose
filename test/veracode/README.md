<pre>
 ______     ______     ______   ______     ______     ______    
/\  == \   /\  ___\   /\  == \ /\  __ \   /\  ___\   /\  ___\ 
\ \  __/   \ \  __\   \ \  _-/ \ \ \/\ \  \ \___  \  \ \  __\
 \ \_\ \_\  \ \_____\  \ \_\    \ \_____\  \/\_____\  \ \_____\ 
  \/_/ /_/   \/_____/   \/_/     \/_____/   \/_____/   \/_____/
  

                    .'.-:-.`.
                    .'  :  `.
                    '   :   '   /
                 .------:--.   /
               .'           `./
        ,.    /            0  \
        \ ' _/                 )
~~~~~~~~~\. __________________/~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
</pre>

#Scanning Repose artifacts with Veracode#

https://analysiscenter.veracode.com


##Unsupported Frameworks (non-blocking)

The following frameworks are used within Repose but are not supported for scanning within Veracode.  Veracode can still
perform a scan of Repose libraries using these frameworks, but will produce an incomplete list of flaws.

Frameworks used by Repose but not supported in Veracode scanning:

   1. Jersey
   2. Jax-RS
   3. Apache Xalan

Repose modules that use 1 or more Veracode unsupported frameworks:

   1. client-auth
   2. client-authorization
   3. core-lib
   4. rate-limiting
   5. translation
   6. utilities


##Per Veracode on the "Unsupported Frameworks" error:

>This message is informational only, which means that your scan proceeds even if your scan request is for an application
that has one or more unsupported frameworks. After the scan of an unsupported framework, Veracode typically produces an
incomplete list of the flaws in the application. These flaws are valid, but because the use of the unsupported
framework(s) can prevent Veracode from creating a complete model of the application prior to scanning, there are parts
of the application that were not scanned, which leads to an incomplete flaw list.
