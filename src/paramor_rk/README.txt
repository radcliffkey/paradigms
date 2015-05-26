
######################################################################
#
# Copyright 2010, Christian Monson
# 
# All Rights Reserved
# 
# Permission to use, copy, modify, and distribute this software 
# (ParaMor) and its documentation for any purpose other than its 
# incorporation into a commercial product is hereby granted without 
# fee, provided that this copyright notice appears in supporting 
# documentation.
# 
# If you wish to receive a license to incorporate this software into 
# a commercial product, please contact Christian Monson at 
# christian.monson@gmail.com.
# 
# CHRISTIAN MONSON DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS 
# SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND 
# FITNESS FOR ANY PARTICULAR PURPOSE. IN NO EVENT SHALL CHRISTIAN 
# MONSON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES
# OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR 
# PROFITS, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE
# OF THIS SOFTWARE.
# 
# ParaMor makes use of a Java library called GNU Trove. GNU Trove is 
# licensed under the Lesser GNU Public License. GNU Trove and its 
# license statements are included with this distribution in the 
# gzipped archive trove-3.0.0a3.tar.gz.
#
######################################################################

----------------
 1. Background
----------------

This file describes how to run the ParaMor v0.1 unsupervised 
morphology induction system. ParaMor was built by me, Christian 
Monson, while a graduate student at Carnegie Mellon University. My 
Ph.D. thesis (Monson, 2009), which you can find on the web, describes 
ParaMor in detail.

The ParaMor code that is bundled with this README is exactly the 
research system that I developed for my thesis. As a research system, 
the code is sometimes rough. The code contains research dead ends 
that, while initially promising, ultimately led nowhere. Moreover, the
code was designed to be used by me personally. While I built an 
interface that I would be able to remember myself, it was not designed
with others in mind.

ParaMor is written in Java. And I recommend you use the newest version
of Java to run ParaMor. I use templates and other newer Java syntax 
that requires at least Java 1.5.

ParaMor runs in a menu driven fashion, with input from the user 
required to run each of ParaMor's sub-tasks. Unfortunately, there is 
not currently a command line mode that allows the user to specify all 
of ParaMor's parameters with command line flags. Nor is there a 
standardized API that specifies Java function calls that accomplish 
specific tasks. 

On the other hand, bundled with this README is the full ParaMor Java
code. So if you wish, you may dive inside the code and interface
directly with it. (Note that to compile the ParaMor source code you
will need to link to GNU Trove, a high-performance collections package
for Java. GNU Trove is included with this distribution in the tarball
trove-3.0.0a3.tar.gz, and is also available from:
http://trove4j.sourceforge.net/).

The above copyright statement specifies just 2 limitation on the use
of ParaMor:

1) You may not incorporate ParaMor into a commercial product. If you 
   intend to make money through the use of ParaMor, please contact me 
   at christian.monson@gmail.com for a commercial license. 

2) Any time you redistribute part or all of ParaMor, please include 
   the above copyright notice as part of the distribution.

I would also ask that, if you use ParaMor in your research, please
cite me (Monson, 2009).


-----------------------
 2. How to Run ParaMor
-----------------------

As you run ParaMor, keep in mind ParaMor's major execution steps. To 
instruct ParaMor to segment a corpus of natural language text into
morphemes in an unsupervised fashion you will:

A. Read in a text corpus from which ParaMor will identify likely 
   paradigms
B. Build a lattice of candidate partial paradigms, or schemes.
C. Search the lattice for paradigm candidates that are most likely to 
   model portions of true paradigms.
D. Cluster partial paradigms into larger structures that more closely 
   model natural language paradigms.
E. Filter out those partial paradigm clusters that are least likely.
F. Segment a corpus using the discovered paradigms.

The ParaMor algorithm consists of a number of individual steps, and 
each step has various parameters that can be set. When running 
ParaMor, each algorithmic step corresponds to an interactive menu. And
each menu lets the user: 

  1) Specify parameter values, and 
  2) Run that algorithmic step. 

If you are basically familiar with how to run ParaMor and just want a
quick refresher on which commands you will need to call in what
sequence, you can skip directly to Section 3 near the end of this 
file.


  2.1. The Commandline
 - - - - - - - - - - - -
 
Although ParaMor runs in a menu driven fashion, I had originally 
envisioned setting parameters in an initialization file. Vestiges of 
this initialization file famework still exist in the code. The most 
important vestige is that, currently, to run ParaMor at all you must 
supply an initialization file on the command line. A sample
initialization file, english.init, is included with this ParaMor v0.1
bundle. To run ParaMor use the command:

java <java_params> -jar ParaMor_v0.1.jar -if <initialization_file>

In this command, <java_params> are command line options directed 
toward the java virtual machine. In particular, if you run large text 
corpora through ParaMor, you will quickly run out of memory. By 
default, Java severely limits the amount of memory that a virtual 
machine may use. To increase the amount of memory available to Java,
use the '-Xmx<amot>' flag where <amt> is an amount of memory. For 
example to give ParaMor 500 MB of memory space use the flag 
'-Xmx500m', or to give ParaMor 3 GB of memory, use '-Xmx3g'. So 
altogether a command to run ParaMor might look like:

java -Xmx3g -jar ParaMor_v0.1.jar -if english.init

This command assumes that java is in your system path and that the
current directory contains both the ParaMor_v0.1.jar and english.init.


  2.2. The Initialization File
 - - - - - - - - - - - - - - - -

To run ParaMor you must use the '-if' command line argument to specify
an initialization file. Included in this ParaMor v0.1 bundle is a 
sample initialization file called english.init. A ParaMor 
initialization file specifies a few basic parameters for the very 
first step of the ParaMor algorithm--namely reading in a text corpus. 

The format of an initialization file is straightforward. Each line 
contains a single command. Each command consists of:

  1) A parameter name and 
  2) A value for that parameter 

separated by a space character. Lines preceded by a '#' character are
comments.

In the english.init initialization file, the first command is a 
'corpus' command:

corpus ./TomSawyer.txt

The 'corpus' command instructs ParaMor where to find the corpus of 
text that ParaMor is to analyze for morphological paradigms. The
TomSawyer.txt file contains the text of Mark Twain's "The Adventures
of Tom Sawyer," which is available for free from the Gutenberg 
Project's webpage (http://www.gutenberg.org/wiki/Main_Page). 

While all the commands that appear in the english.init file can be
overridden using ParaMor's interactive menu system, ParaMor v0.1
*WILL* *NOT* run without including a 'corpus' command in the
initialization file (even if you later override the specified corpus
using the menu system).


  2.3. ParaMor's Interactive Menus
 - - - - - - - - - - - - - - - - - -

    2.3.1. The Root Menu
   - - - - - - - - - - - -

When you start ParaMor from the command line, ParaMor reads and 
processes the initialization file. As ParaMor consumes each command in
the initialization file, ParaMor prints to the screen a status message
indicating the success, or failure, of that command. Once ParaMor has 
finished processing the initialization file, ParaMor presents the user
with the 'Root Menu'. Like all of ParaMor's menus, the root menu is
simply text that prints to the screen. 

The Root Menu looks like this:


     Root Menu

     Select an Action:
     -----------------

       <SC> <S>et the <C>orpus.  Read in a corpus.  Set the corpus' settings.

       <CN> <C>ompute a Morphology Scheme <N>etwork

       <INE> <I>nteractive <N>etwork <E>xploration

       <SP>  <S>earch and <P>rocessing.  Search a Morphology Scheme Network
          And process any results of a search.

       <Q>uit


From the Root Menu the user may enter one of 5 commands. Each of these
commands takes the user to another interactive menu. Note that none of
ParaMor's menu commands are case sensitive. The Root Menu commands 
are:

1. <SC>  to read in a corpus
2. <CN>  to compute a scheme network
3. <INE> to interactively explore a scheme network
4. <SP>  to proceed to the meat of the ParaMor algorithm
5. <Q>   to quit ParaMor

To invoke any one of these 5 commands, simply type the letters that 
appear between the angle brackets, '< >' at the command prompt. For 
example, to read in a corpus type the two characters: 'SC' or 'sc' 
(The menu commands are not sensitive to case).

Remember from Section 1., above, that we want to instruct ParaMor to:

A. Read in a text corpus from which ParaMor will identify likely 
   paradigms
B. Build a lattice of candidate partial paradigms, or schemes.
C. Search the lattice for paradigm candidates that are most likely to 
   model portions of true paradigms.
D. Cluster partial paradigms into larger structures that more closely 
   model natural language paradigms.
E. Filter out those partial paradigm clusters that are least likely.
F. Segment a corpus using the discovered paradigms.

The <SC> command on the Root Menu directly corresponds to item A. 
Similarly, the <CN> command will instruct ParaMor to build a lattice 
of partial paradigms, item B from above. And the <SP> command will 
allow ParaMor to accomplish items C through F.

And then there are two final commands on the 'Root Menu': the <Q> 
command and the <INE> command. While the <Q> command simply exits 
ParaMor, the <INE> command is more interesting. Invoking the <INE> 
command takes the user to a system of interactive menus where you can 
walk though a network of partial paradigms by hand. If you are curious
about the qualitative properties of morphology scheme networks, by all
means take a moment to stroll through the output of the <INE> command.
However, this tutorial is not going to describe the <INE> menu 
subsystem in detail, but instead focus on the steps that are necessary
for ParaMor to build a paradigm model and then segment a corpus into
morphemes using that model. Note that the <INE> command can only be 
called after a network of partial paradigms has been built by calling 
the <SC> and <CN> commands.


    2.3.2. The Corpus Menu
   - - - - - - - - - - - - -

Typing 'SC' from the 'Root Menu' (Section 2.3.1) and pressing Enter 
will take you to the 'Corpus Menu' which will accomplish Task A: Read 
in a text corpus (See Section 1). 

At the top of the 'Corpus Menu', ParaMor prints the current values of 
various parameters that effect how ParaMor processes a text corpus. 
Beneath the printout of parameters, ParaMor presents a number of 
commands or actions. The commands are formatted similarly to the 
commands given on the Root Menu. Most of the commands on the Corpus 
Menu change the setting of one of the parameters that affect corpus 
processing. A few of the commands tell ParaMor to read in a corpus. 

The default settings of the 'Corpus Menu' parameters, some of which
are specified in the english.init initialization file, are reasonable 
settings. But in case you are interested, an explanation of each 
parameter-changing command appears in the Section 2.3.2.1. Commands
that actually read in the corpus are explained in Section 2.3.2.2.


      2.3.2.1. Commands that Change Parameters
     - - - - - - - - - - - - - - - - - - - - - -

<SetCP> : <Set> <C>orpus <P>ath : This command changes the corpus that
  ParaMor will read in. This menu command overrides the 'corpus' 
  command found in the initializations file. Note that although the 
  <SetCP> menu command can override the initializations file command, 
  ParaMor will currently NOT run without some corpus being specified 
  in the initializations file.

<SetL> : <Set>ting the corpus <L>anguage effects ParaMor's 
  tokenization of the input corpus. For example, the 'English' and 
  'Spanish' language settings explicitly enumerate the characters that
  ParaMor expects to occur in English and Spanish words respectively. 
  ParaMor will surround every character outside the hardcoded alphabet
  with token boundaries. 

  Unfortunately, explicitly listing alphabet characters runs into 
  problems with English words like naïve--where did that 'ï' come 
  from! Eventually, I decided to just tokenize on whitespace. 
  Whitespace tokenization is activated by setting the language to 
  'GENERIC', choice number 6 in the sub-menu that is activated when 
  you invoke the <SetL> command. Note that generic whitespace 
  tokenization is not going to work for languages like Vietnamese or 
  Thai where the writing system is alphabetic but words are not 
  space-delimited. However, if your application requires special
  tokenization, you can preprocess your data to be space delimited 
  before passing it on to ParaMor. 

  In general, I recommend using the GENERIC whitespace segmentation 
  method.
  
<SetD> : <Set> throwing out <D>igits : This command instructs ParaMor 
  to ignore or retain tokens that contain a numeric character, i.e. 0,
  1, 2, 3, 4, 5, 6, 7, 8, or 9. Numbers and other strings that contain
  digit characters, like URLs, product names, etc., do not participate
  in morphological paradigms. In fact, in numbers, digit characters 
  can, by definition, occur in any and all possible sequences. So, 
  unless the particular writing system or transliteration you are 
  using represents some phoneme with a numeric character, I recommend 
  setting 'throw out digits' to true.

<SetE> : <Set> the corpus <E>ncoding : NOT IMPLEMENTED. Running this 
  command just tells the user that this command is not implemented. 

  ParaMor assumes that all corpora you feed it will be in the utf8 
  encoding of Unicode. 
  
<SetCS> : <Set> <C>ase <S>ensitivity : This command instructs ParaMor 
  to ignore (false) or to retain (true) case information when reading 
  a corpus.

<SetTL> : <Set> <T>ype <L>ength : As described in my thesis (Monson, 
  2009), when ParaMor attempts to learn paradigm structures over short
  word types, accidental string similarities swamp the signal from 
  true paradigms. The <SetTL> command sets a parameter that admits to 
  ParaMor's paradigm-induction wordlist only types that contain more 
  characters than a specified threshold. 

  Entering 'SetTL' will take you to a 'Positive Integer Query Menu'. 
  At this integer query menu, type a length requirement. As the 
  on-screen instructions state, only word corpus types that are LONGER
  than the threshold that the user specifies will participate in 
  paradigm induction. (Despite the submenu's name, you can specify a 
  threshold of 0 to not exclude any types from paradigm induction). 

  My thesis suggests setting the type length parameter to 5: At a 
  setting of 5, all words from which paradigms are induced contain at 
  least *6* characters.

<SetISGML> : <Set> <I>gnore <SGML> tags : If this flag is set to true,
  all corpus text that appears between a pair of pointy brackets, '<' 
  and '>', will be ignored. ParaMor does *not* have a true SGML parser
  embedded inside it. When set to 'ignore' SGML, ParaMor will look for
  any '<' character and immediately start ignoring input until it
  encounters any '>' character.

<SetTokTS> : <Set> <Tok>ens <T>o <S>kip
<SetTokTR> : <Set> <Tok>ens <T>o <R>ead
<SetTypTS> : <Set> <Typ>es  <T>o <S>kip
<SetTypTR> : <Set> <Typ>es  <T>o <R>ead : These four commands instruct
  ParaMor on what section of the input corpus to use for paradigm 
  induction. You can tell ParaMor to read a particular section of a 
  corpus either by counting tokens, or else by counting unique types. 
  Use the <SetTokTS> and <SetTypTS> commands to skip over the first N 
  tokens or unique types respectively. And then use the <SetTokTR> and
  <SetTypTR> commands to read in a set number of tokens, or types.

  Since ParaMor reduces a list of tokens to a list of types, it is 
  most typical to read types as opposed to tokens. For example, in all
  my Morpho Challenge experiments I invoke the <SetTypTR> command and 
  enter '50000' when ParaMor prompts for the number of types I would 
  like to read from the corpus file. (In fact, reading 50,000 types is
  ParaMor's default, so you probably don't need to touch this 
  parameter at all).

  A few additional notes:

    1) Although ParaMor typically induces paradigms from a corpus of 
       around 50,000 unique types, ParaMor will then take the induced 
       paradigms and apply them to segment as many types as is 
       desired (See Section 2.3.4.1.4). At the Morpho Challenge 
       competitions, ParaMor has segmented 2 million unique word 
       forms, or more, for individual languages.

    2) It is also possible to set the number of types (or tokens) you 
       wish ParaMor to read by using commands in the initializations 
       file (See Section 2.2.).

    3) Invoking the <SetTypTR> or <SetTokTR> command when ParaMor has 
       already been instructed to read a specific number of types or 
       tokens will override the previously specified type or token 
       count that was to be read. Similarly, invoking either the 
       <SetTypTS> or the <SetTokTS> commands will override any 
       previous setting for the number of tokens or types that was to 
       be skipped.

    4) To read an entire corpus, enter zero ('0') for the number of 
       types or tokens you wish ParaMor to read. Instructing ParaMor 
       to read zero tokens (or types) will actually force ParaMor to 
       read *all* available tokens (and types) in a corpus.

    5) The parameter listing given at the top of the 'Corpus Menu' may
       state that ParaMor is 'Skipping: 1st Token'. This is an 
       off-by-one bug. Unless you explicitly invoke the <SetTypTS> or
       the <SetTokTS> commands, no tokens are being skipped. 


      2.3.2.2. Commands that Read in (or write out) the Corpus
     - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

<SetWOEC> : <Set> <W>rite <O>ut <E>xact <C>orpus : With all the 
  options of ignoring/retaining case information, skipping or 
  including tokens containing digits, skipping word types that are at 
  least as short as a threshold, optionally skipping SGML-like 
  strings, etc., it can be useful to know *exactly* what tokens of the
  corpus it was that ParaMor read in. If you invoke the <SetWOEC> 
  command, you will be prompted to supply a filename to which the 
  exact sequence of tokens that contributed to ParaMor's 
  paradigm-induction corpus will be written. The actual writing of the
  'WOEC' file will occur when you call the <RC> command, described 
  next. 

<RC> : <R>ead in a <C>orpus : This command does the actual reading in 
  of the specified corpus of text. This command prints off its 
  progress at reading types and tokens as it progresses through the 
  corpus. Once the corpus has been read, ParaMor returns to the 'Root 
  Menu', described in Section 2.3.1.

<WCV> : <W>rite out the <C>orpus <V>ocabulary that was read in : The 
  <WCV> command serves a similar function as the <SetWOEC> command: 
  Where the <SetWOEC> command prints out the exact read-in corpus 
  *token* by *token* (skipping numbers etc.), the <WCV> command prints
  out each unique *type* that ParaMor read during the <RC> command.

  Note that the <WCV> command can only be invoked **after** the <RC> 
  command has been run. But when you call the <RC> command, you are 
  dumped back out at the 'Root Menu'. To call the <WCV> command you 
  must return to the 'Corpus Menu' from the 'Root Menu' by reinvoking 
  the <SC> (i.e. <S>et <C>orpus) command. 

<R> : <R>eturn to the Root Menu : Takes the user back to the 'Root 
  Menu' without reading in a corpus of text. (Useful after calling the
  <WCV> command).


    2.3.3. The Create Network Menu
   - - - - - - - - - - - - - - - - -

Typing 'CN' from the 'Root Menu' (Section 2.3.1) and pressing Enter 
will take you to the 'Create Network Menu' which will accomplish 
Task B from Section 1: Build a lattice of partial paradigms. Prior to 
building a lattice of partial paradigms you must have read in a corpus
of text (Section 2.3.2.).

Although partial paradigms are conceptually simple, efficiently 
building a lattice of partial paradigm structures was one of the most 
technically challenging aspects of the ParaMor morphology induction 
algorithm (see Section 3.2.4. of my thesis: Monson, 2009). The 'Create 
Network Menu' retains vestiges of the many directions I tried before 
discovering the dynamic network generation that is now used. 
Consequently, while the 'Create Network Menu' presents several 
different commands to the user, the only command of any utility is the
<CN> command:

<CN> : <C>reate a <N>etwork : From the corpus of text that ParaMor has
  read (see Section 2.3.2. of this README) this command builds data 
  structures that mark the upper and lower boundaries of a network of 
  partial paradigms. More specifically, ParaMor builds the set of 
  most-specific schemes and the set of level 1 schemes (where 'scheme'
  is another name for 'partial paradigm', see Monson, 2009). During 
  ParaMor's search through the network of partial paradigms (Task C, 
  described in Section 2.3.4.1 of this README), ParaMor will use the 
  upper and lower boundaries to construct the specific individual 
  partial paradigms, or schemes, that the search actually visits. 

  ParaMor prints a variety of progress information as it constructs
  the upper and lower boundaries of the network. Once boundary
  construction completes, ParaMor takes the user back to the 'Root 
  Menu', Section 2.3.1.


    2.3.4. The Search and Processing Menu
   - - - - - - - - - - - - - - - - - - - -

Typing 'SP' from the 'Root Menu' (Section 2.3.1) and pressing Enter 
will take you to the 'Search and Processing Menu'. Before arriving at 
the 'Search and Processing Menu', however, you need to have called the
<S>et <C>orpus and the <C>reate <N>etwork commands from the 'Root 
Menu'. The <S>et <C>orpus command will have read in a corpus of text, 
as described in Section 2.3.2., while the <C>reate <N>etwork command 
will have built a network of partial paradigms, as described in 
Section 2.3.3. 

But now you are at the meaty 'Search and Processing Menu'; from which 
you can accomplish Tasks C through F from Section 1, namely:

C. Search the lattice for paradigm candidates that are most likely to 
   model portions of true paradigms.
D. Cluster partial paradigms into larger structures that more closely 
   model natural language paradigms.
E. Filter out those partial paradigm clusters that are least likely.
F. Segment a corpus using the discovered paradigms.

Serving so many functions, the 'Search and Processing Menu' is the 
most complex menu of ParaMor. Additionally, the 'Search and Processing
Menu' has more deadends and unneeded commands than any other menu. 
Section 2.3.4.1. will walk through the commands on this menu that 
*are* needed to accomplish tasks C through F.

But first, I will mention the information that the 'Search and 
Processing Menu' displays below the menu title. The 'Search and 
Processing Menu', as well as most of its major submenus, prints 
information that describes the current steps that ParaMor has taken 
toward accomplishing Tasks A through F of Section 1. 

The first time you arrive at the 'Search and Processing Menu' you will
have read in a corpus and built a morphology scheme network over that 
corpus (Tasks A and B). And so the 'Search and Processing Menu' prints
information relating to the corpus that you have read in. The printed 
information includes: the name of file that ParaMor read-in as a 
corpus of text, the number of types or tokens that ParaMor read in, 
whether or not you have skipped tokens that contain numbers, etc. 

Then, beneath the corpus information, ParaMor prints the sentence "No 
Search Steps have yet been performed with this Search Batch". Later, 
as you execute commands that accomplish Tasks C through F, this 
sentence will be replaced with the details of each search, clustering,
or filtering command that you execute.

Note that after you have run several search, clustering, or filtering 
commands, ParaMor will be printing a significant amount of information
to the screen. The printed information may push the title of the 
current menu off to top of your command line terminal. If you are ever
confused over what menu you really are at, just scroll up.


      2.3.4.1 The Commands of the Search and Processing Menu
     - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        Vital Commands
       - - - - - - - - -

By invoking the following 5 commands in the following order from the 
'Search and Processing Menu' you can accomplish Tasks C through F as 
described in Section 1.

<BUS> : <B>ottom <U>p <S>earch : This command takes the user to the 
  'Bottom Up Search Menu' which sets parameters for and then runs the 
  core paradigm-search algorithm of ParaMor. The paradigm-search 
  algorithm is exactly 'Task C: Search the lattice for paradigm
  candidates that are most likely to model portions of true paradigms'
  (As described in Section 1). The 'Bottom Up Search Menu' is 
  described further in Section 2.3.4.1.1.

<C> : <C>luster : This command accomplishes 'Task D: Cluster partial 
  paradigms into larger structures that more closely model natural
  language paradigms' (See Section 1). Invoking the <C> command takes 
  the user to the 'Clustering Menu'. In addition to completing Task D,
  with the right parameter settings, the 'Clustering Menu' can also 
  begin 'Task E: Filter out those partial paradigms that are least 
  likely.' The 'Clustering Menu' is presented in full in 
  Section 2.3.4.1.2.

<MBTFLFC> : <M>orpheme <B>oundary <T>oo <F>ar <L>eft <F>ilter applied 
  to <C>lusters : It takes several menu commands to accomplish 
  Section 1's Task E: The filtering of ParaMor's initially selected 
  clusters. Task E was most likely begun with the <C> command 
  (described immediately above and in Section 2.3.4.1.2.), and the 
  <MBTFLFC> command applies an additional filter. The <MBTFLFC> filter
  looks for candidate partial paradigms in which ParaMor likely 
  hypothesized a morpheme boundary that falls internal to a true stem 
  (i.e. a <M>orpheme <B>oundary that is placed (<T>oo <F>ar) to the 
  <L>eft of a true suffix boundary). 

  The letter sequence that invokes the <MBTFLFC> command, namely 
  'MBTFLFC', is similar to the letter sequences of several other 
  commands on the 'Search and Processing Menu'. A separate command, 
  <MBTFRFC>, discussed next, looks for morpheme boundaries placed too 
  far to the **right**, or suffix internally; And two additional, 
  obsolete, commands can only be run before the clustering command 
  (<C>). These two outdated commands lack the final 'C' character in 
  their command name.

  When you invoke the <MBTFLFC> command, ParaMor's menu system moves 
  to the 'Morpheme Boundary Too Far Left Filter' menu. The details of 
  this menu are presented in Section 2.3.4.1.4.

<MBTFRFC> : <M>orpheme <B>oundary <T>oo <F>ar <R>ight <F>ilter applied
  to  <C>lusters : This command is yet another filtering command that 
  accomplishes (and completes) Task E as described in Section 1 of 
  this README. Like the <MBTFLFC> command just discussed, the 
  <MBTFRFC> command looks for ill-placed morpheme boundaries. This 
  time, morpheme boundaries are sought that are likely placed internal
  to *suffixes* (i.e. are to the right of a true morpheme boundary). 
  
  As discussed in connection with the <MBTFLFC> command, the letter 
  sequence to invoke the <MBTFRFC> command is very similar to the 
  letter sequences for several other commands--so be careful. The 
  <MBTFRFC> command is further detailed in Section 2.3.4.1.4, jointly 
  with the <MBTFLFC> command. 

<Seg> : <Seg>ment : This command accomplishes 'Task F: Segment a 
  corpus using the discovered paradigms.' Calling the <Seg> command 
  will take you to the 'Segmentation Menu' which is described in full 
  in Section 2.3.4.1.5. 


        Useful Commands
       - - - - - - - - -

Interspaced between your invocations of the <BUS>, <C>, <MBTFLFC>, 
<MBTFRFC>, and <Seg> commands, in that linear order, you may find it 
useful to call the following commands which also appear on the 'Search
and Processing Menu':

<SetOut> : <Set> the prefix for the <Out>put files : The <WR>, <WCR>, 
  and <WSB> commands, discussed momentarily, allow the user to write 
  out intermediate results from, and to save the state of, a ParaMor 
  run. The <SetOut> command allows the user to specify a location at 
  which to write out these result and state files. Note however, that 
  the <WR>, <WCR>, and the <WSB> commands also let you specify the 
  write location when you invoke them individually.

<TCFC> : <T>ype <C>overed <F>ilter applied to <C>lusters : As 
  described further in Section 2.3.4.2, the <TCFC> command invokes a 
  particular type of paradigm filtering that fulfills part of Task E 
  (See Section 1). However, the 'type covered' filtering that the 
  <TCFC> command computes is also built directly into ParaMor's 
  clustering algorithm and so you likely will not need to call the 
  <TCFC> command explicitly. See Section 2.3.4.1.3 for details. 

  Also, please don't be confused by the presence of both a <TCFC> as 
  well as a <TCF> command. The <TCF> command is outdated.

<WR> : <W>rite the <R>esults of the current search batch : The <WR> 
  command instructs ParaMor to first 'evaluate' the partial paradigms,
  i.e. schemes, that ParaMor selected during the bottom-up search 
  procedure (i.e. the <BUS> command) and to then write out the raw set
  of selected paradigm schemes along with the results of the 
  'evaluation'. The <WR> command must be invoked after searching the 
  network of candidate partial paradigms with the <BUS> command, but 
  before clustering the initially selected partial paradigms with the 
  <C> command. 

  The reason I placed the word 'evaluation' in quotes in the preceding
  paragraph is that paradigm evaluation only works when you are 
  analyzing Spanish data and when the language was set as 'Spanish'
  when the corpus-to-analyze was first read in, see Section 2.3.2. 
  When analyzing non-Spanish data, ParaMor will perform and report on 
  a vacuous evaluation. (On the other hand, if you *are* analyzing 
  Spanish data, ParaMor's paradigm evaluation will report the 
  percentage of the suffixes of each major paradigm of Spanish that 
  is covered by some partial paradigm that ParaMor's search procedure 
  selected). 

  Although ParaMor's 'evaluation' of the scheme suffix coverage of the
  paradigms of your language will most likely be vacuous, the <WR> 
  command is still useful because it will print to a file, in human 
  readable format, the set of partial paradigms that ParaMor selected 
  as a result of searching the network of partial paradigms with the 
  <BUS> command. Examining the paradigms that ParaMor selects can be a
  valuable sanity check that ParaMor is doing something reasonable for 
  your language.

  Here is the procedure to write out the file of partial paradigms 
  (i.e. schemes): When you execute the <WR> command, ParaMor will 
  first inform you that it is ''evaluating' the current search batch'.
  Then, once you press [Enter], you will be prompted to specify a 
  'fully qualified file name prefix' (i.e. a directory and a file name
  prefix, for example, 'c:/myDirectory/someSchemes'). ParaMor will 
  write out 3 files to the designated directory with the designated 
  file name prefix. (In this example, the three files will be written 
  to the 'c:/myDirectory/' directory, and the names of each of the 
  three files will begin with 'someSchemes'). The partial paradigm 
  schemes that ParaMor selected during the bottom-up search will be in
  the file that ends in '-schemes.txt' (i.e. in 
  'c:/myDirectory/someSchemes-schemes.txt').

<WCR> : <W>rite <C>luster <R>esults : The <WCR> command is very 
  similar to the <WR> command. Like the <WR> command, the <WCR> 
  command (vacuously) 'evaluates' ParaMor's current set of partial 
  paradigms (which are now clusters of schemes); and then, more 
  usefully, the <WCR> command prints the paradigm clusters themselves 
  in a human-friendly format. Moreover, the actual process of running 
  the <WCR> command is very similar to the process described for the 
  <WR> command (see the description under the <WR> command). The only 
  reason the <WCR> command is separate from the <WR> command is that, 
  internally, ParaMor represents raw paradigm schemes in a separate 
  class data structure from that which represents clusters of schemes.
  Hence, the the <WCR> command may only be invoked **after** you  have
  run the <C> command to cluster ParaMor's initially selected partial 
  paradigms. 

<WSB> : <W>rite a <S>earch <B>atch : This command saves ParaMor's 
  current state to disk in a computer-readable format. While the <WR> 
  and <WCR> commands (above) print ParaMor's partial paradigms in a 
  human-readable format, the <WSB> command saves ParaMor's paradigms 
  in a format that ParaMor itself can understand. 

  The <WSB> command is useful when, for example, you run ParaMor's 
  search over a large corpus with a particular setting of parameters 
  using the <BUS> command. Invoking <WSB>, you can then save the 
  results of ParaMor's search and return at some later date with the 
  <RSB> command, described next, to read in the saved state and 
  continue analysis of the large corpus without repeating the 
  bottom-up search step. 

  The <WSB> command will write out the program state to a gzipped file
  that ends with the string '-serializedSearchBatch.gz'.

 <RSB> : <R>ead a <S>earch <B>atch : This command reads in a ParaMor 
  program state that was written out using the <WSB> command. After 
  successfully reading in a saved program state with <RSB>, it is just
  like you had run ParaMor over a corpus from scratch. All the same 
  commands are valid over a saved and read-in ParaMor run as for a run
  in which each processing command was separately hand-executed.

  Just a minor warning: Don't be surprised when the <RSB> command 
  re-computes the morphology scheme network that was originally 
  computed from the 'Create Network Menu', Section 2.3.3. The <WSB> 
  command does not write out the original morphology scheme network 
  because the circular reference structure of the network confuses 
  Java's automatic serialization. But network creation is fast, a few 
  seconds for a corpus of millions of unique types.

  Also, the <RSB> command expects to read a gzipped file, but the 
  <RSB> command will automatically supply the '.gz' extension. Hence, 
  if you want to read a file called 
  'paraMorState-serializedSearchBatch.gz', instruct the <RSB> command
  to read 'paraMorState-serializedSearchBatch', with no '.gz' file 
  extension


        Outdated Commands
       - - - - - - - - - -

Having discussed the useful commands on the 'Search and Processing 
Menu,' you may entirely ignore the following:

  <MLF>, <MBTFLF>, <MBTFRF>, <TCF>, <CTBSF>, <WCWFTS>

All of these commands are outdated and unsupported. Some of these 
commands were designed to apply filtering techniques to partial 
paradigm schemes *before* they were clustered. But in experiments that
were never published, I determined that (at least in Spanish) 
clustering unfiltered schemes and then filtering the clusters 
prevented ParaMor from discarding true but rare suffixes. Others of 
these outdated commands call filtering techniques that ultimately 
proved unhelpful. And some of these commands even I do not remember 
what they do. Don't use them. Ignore them. And don't get confused by 
the unfortunate similarity between the names of some of these 
commands, and the names of other legitimate commands.


        2.3.4.1.1 Bottom Up Search Menu
       - - - - - - - - - - - - - - - - -

Executing the <BUS> command from the 'Search and Processing Menu'
(Section 2.3.4.1) takes you to the 'Bottom Up Search Menu'. From the 
'Bottom Up Search Menu' you can instruct ParaMor to search a 
morphology scheme network for likely partial paradigms--thus 
completing Task C described in Section 1. 

ParaMor searches the scheme network for likely partial paradigms in a 
bottom-up fashion. At the bottom of the network are schemes which 
contain just a single candidate suffix. ParaMor considers each 
single-suffix scheme in turn, attempting to build a more full paradigm
by moving upward through the network to schemes which contain 
successively larger sets of candidate suffixes. If you would like a 
more complete understanding of the procedure that ParaMor uses to
search partial paradigm networks, please consult chapter 3 of my 
thesis (Monson, 2009).

Before presenting the commands that constitute the 'Bottom Up Search 
Menu', ParaMor prints:

  1. Details summarizing the corpus that has been read in, and
  2. The current settings of all the parameters that govern ParaMor's 
     bottom-up search. 

The majority of the commands on the 'Bottom Up Search Menu' effect 
changes to the parameter settings for search. Despite the wide array 
of parameters that the 'Bottom Up Search Menu' presents , it is safe 
to leave the search parameters at their default values. In fact, I do 
not guarantee that all of the parameters can safely be changed.

The most important parameters to note are that, by default: 

  1) 'Vertical Metric' is set to 'Ratio' : The 'Vertical Metric' is 
     the measure ParaMor uses to decide when to move upward during a 
     bottom-up search. The 'Ratio Vertical Metric' computes the ratio
     of the number of candidate stems in the parent scheme to the 
     number of stems in the current scheme.

  2) 'Vertical Metric Cutoff' is set to '[0.25]' : With this setting, 
     ParaMor will move upward though the partial paradigm scheme 
     network only when a parent scheme retains at least a quarter of 
     the candidate stems found in the current scheme.

  3) 'Require |Stems| > |Affixes|' is set to 'true' : With this 
     setting, ParaMor will only move upward through the scheme network
     if the number of candidate stems in the parent scheme is greater 
     than the number of candidate suffixes in that scheme.

Once you are satisfied with the parameter settings, you may perform 
the actual search by typing 'S' to execute the <S>earch command (and 
then confirming that you want to search with 'y' at the prompt). 

The first thing that ParaMor's search does is sort all the 'candidate 
seeds', where a candidate seed is a scheme with exactly one candidate 
suffix. ParaMor sorts the seeds in decreasing order according to the 
number of candidate stems (or 'contexts') in that scheme. Then, 
beginning with the scheme with the largest number of contexts, ParaMor
attempts to grow larger partial paradigms by successively adding 
additional candidate suffixes. To indicate progress, ParaMor prints to
the screen the search paths that result from a subset of the seed 
schemes.

On my MacBook Pro 2.4 GHz Intel Core 2 Duo with 4 GB of memory, 
ParaMor's bottom up search over one corpus of 50,000 unique 
frequent Spanish words took 5 minutes and consumed 1.5 GB of memory.


        2.3.4.1.2 Clustering Menu
       - - - - - - - - - - - - - -

Executing the <C> command from the 'Search and Processing Menu' 
(Section 2.3.4.1) takes you to the 'Clustering Menu'. From the 
'Clustering Menu' you can instruct ParaMor to 'cluster partial 
paradigms into larger structures that more closely model natural 
language paradigms', Task D as described in Section 1. 

ParaMor's clustering algorithm merges candidate paradigms that were 
selected during the bottom-up search phase (Section 2.3.4.1.1). 
Clustering proceeds in a bottom-up fashion. At each step of 
clustering, ParaMor forms a new candidate paradigm from that pair of 
candidate paradigms that are most 'similar'. The measure that is used 
to gage similarity is one of the parameters that you can set on the 
'Clustering Menu'.

Before presenting the commands that constitute the 'Clustering Menu', 
ParaMor prints:

  1. Details summarizing the corpus that has been read in,
  2. Information about previous commands (i.e. bottom-up-search) that
     have been executed, and
  3. The current settings of all the parameters that govern ParaMor's 
     clustering algorithm. 

With these details presented, the 'Clustering Menu' presents a wide 
variety of parameters whose settings can be changed. But unless you 
are processing a non-standard corpus you can leave the parameters at 
their default settings. 

Here is a description of the menu commands that affect parameters of
ParaMor's clustering algorithm:

<SetCSA> : <Set> <C>alculate <S>imilarity <A>s' : Default: 'COSINE' : 
  This parameter specifies what set-similarity measure ParaMor uses 
  when comparing the sets of candidate stems and candidate suffixes 
  found in a pair of scheme-clusters. The formula for the default 
  cosine similarity metric for two sets X and Y is: 
  |X intersect Y| / sqrt(|X|*|Y|)

<SetCWRT> : <Set> <C>luster <W>ith <R>espect <T>o : 
  Default: 'AFFIX_STEM_PAIRS' : This parameter specifies what sets 
  exactly it is that ParaMor calculates a similarity measure over 
  during clustering. The default, 'AFFIX_STEM_PAIRS', causes ParaMor 
  to construct, for each scheme C, the cross-product of the stems in C
  and the affixes in C. Then the similarity of two schemes, C1 and C2,
  is calculated as the similarity of the cross-product set of C1 and 
  that of C2. Meanwhile, the set of affix-stem pairs that support an 
  arbitrary cluster L that was formed by merging a set of schemes S,
  is defined as the union of the cross-product affix-stem sets of the
  schemes in S. 

  Note that using the cross-product set of stems and affixes in a 
  scheme C is equivalent to using the set of morpheme-boundary-
  annotated surface words that underlie C. In my thesis 
  (Monson, 2009), I use the term morpheme-boundary-annotated words. 

<SetFACT> : <Set> <F>ilter <A>t <C>luster <T>ime : Default: 'false' : 
  Deprecated, DO NOT MODIFY. When set to false, this parameter has no 
  effect on ParaMor's clustering algorithm. This parameter was a 
  research direction that didn't pan out.

<SetDC> : <Set> <D>iscriminative <C>lustering : Default 'false' : 
  Deprecated, DO NOT MODIFY. The function of this parameter has been 
  replaced with the 'Network Based Discriminative Clustering' 
  parameter. Leave this parameter in the 'false' state. When set to 
  false, this parameter will have no effect on the clustering 
  procedure.

<SetNBDC> : <Set> <N>etwork <B>ased <D>iscriminative <C>lustering : 
  Default: 'true' : AND THE DEPENDENT SUB-PARAMETER
<SetL2SMHNS> : <Set> <L>evel <2> <S>cheme <M>ust <H>ave at least <N> 
  <S>tems : Default: '1' : This pair of parameters is a key adaptation
  of standard vanilla bottom-up agglomerative clustering to the 
  clustering of scheme-paradigms. According to Jaime Carbonell, my 
  thesis advisor, clustering is called 'discriminative' whenever a 
  restriction is placed on clustering that no cluster may contain a 
  pair of items that is too dis-similar. 

  ParaMor applies the following discriminative clustering criterion 
  during scheme clustering: No cluster may be formed that would 
  include a pair of candidate suffixes F1 and F2 that were never 
  observed to attach to a common stem to form a surface word. 
  Restated, if cluster L1 contains F1 and cluster L2 contains F2, if 
  there does not exist a stem T such that both T.F1 and T.F2 are words
  in the input corpus then L1 may not be merged with L2.

  More generally, to merge the clusters L1 and L2, ParaMor requires
  that there exist at least N shared stems for each pair of suffixes: 
  F1 in L1, F2 in L2. Where, N is set using the <SetL2SMHNS> command.
  By default, N is simply 1.

  In natural language morphology it is frequently the case that 
  distinct paradigms contain affixes that have identical surface 
  forms--think of English nouns which take 's' for plural, and English
  verbs which use the same 's' to mark 3rd Person Singular. The 
  purpose of ParaMor's discriminative clustering is to prevent 
  distinct paradigms from merging when they appear similar to the raw 
  similarity measure (e.g. cosine of stem-affix pair sets). Even if 
  the paradigms P1 and P2 reuse the same surface affix form (and even 
  if some surface stem, t, happens to occur in both the P1 and the P2 
  paradigms), P1 and P2 will almost certainly contain unique affixes 
  as well. In English, for example, noun stems don't take '-ing' 
  endings, and verb stems don't take possessive -'s; And consequently 
  '-ing' and '-s' will likely have no stem in common. 

  (OK, you are right, because English easily converts nouns into 
  verbs, many stems in English do take both nominal and verbal endings
  ('the chair's cushion' vs. 'chairing the meeting'). But actually, 
  discriminative clustering still works even in English because of 
  more rare derivational endings. Consider the verb-to-noun suffix 
  '-ment' (i.e. enlightenment) and the noun-to-adjective suffix '-al' 
  (i.e. accidental). Here, *enlightenal and *accidentment are 
  non-words, the suffixes -ment and -al will likely not share any stem
  in most English corpora and the noun and verb paradigms of English
  will be kept separate).

  The name of the 'Network Based Discriminative Clustering' parameter
  comes from its implementation, which revisits the scheme network 
  that was built for ParaMor's bottom-up search procedure 
  (Section 2.3.4.1.1). Here, ParaMor uses the scheme network to check
  the number of stems that pairs of suffixes have in common.

  Normally, you will leave the 'Network Based Discriminative 
  Clustering' and the 'level 2 Scheme must have >=N Stems' at their
  default values of 'true' and '1'.

<SetAICR> : <Set> <A>ffixes <I>n <C>ommon <R>equired : 
  Default: 'false' : If set to 'true', this parameter will only merge 
  a pair of scheme-clusters if the intersection of the affixes of the 
  two scheme-clusters is non-empty. That is, the scheme (or cluster) 
  pair must have at least one affix in common. In unpublished 
  experiments, we investigated using this *affixes*-in-common 
  restriction while measuring similarity over the sets of *stems* in 
  the clusters. This technique proved not as useful as simply 
  measuring similarity over the cross-product sets of affix-stem 
  pairs. Leave in the default state of 'false'.

<SetAICF> : <Set> <A>ffixes <I>n <C>ommon <F>orbidden : 
  Default: 'false' : If set to 'true' this parameter will not permit a
  pair of scheme-clusters to merge if the two clusters share even a 
  single affix. The idea was to quickly join sets of schemes that 
  share stems but that have very different sets of suffixes--this was 
  a bad idea, or rather an interesting exploratory direction that was 
  useful to characterize scheme clustering, but not useful to actually
  model natural language paradigms. Leave in the default state of
  'false'.

The next four commands are closely intertwined:

<SetCTCC> : <Set> <C>hild <T>ype <C>overed <C>utoff : Default: '37' :
  ParaMor prints the value of this parameter under the title: 'At 
  least one child must cover >N Types'. ParaMor will only merge a  
  pair of scheme-clusters L1 and L2 if at least one of L1 and L2 is 
  supported by more than this parameter's worth of unique types (where
  the types are *not* marked for morpheme boundaries). This parameter
  is useful because ParaMor's initial scheme selection procedure 
  selects many schemes supported by very few surface types. Most of 
  these small schemes are junk, a few contain true but rare affixes. 
  This parameter helps prevent ParaMor from merging all these junk 
  schemes together into hideously wrong pseudo-paradigms. 

<SetUMC> : <Set> <U>se <M>erge <C>redits : Default: 'true' : AND THE 
  DEPENDENT SUBPARAMETER:
<SetPMC> : <Set> <P>ositive <M>erge <C>redits : Default: '1' : Let L 
  be a cluster; Let N be the value of the parameter 'At least one 
  child must cover >N Types' (see above); And let K be the value of 
  the '# of Positive Merge Credits' parameter. If the 'Use Merge 
  Credits' parameter is set to 'true', then for each scheme in L that 
  is supported by more than N unique types, L may contain at most K 
  schemes that are supported by N or fewer unique types. In 
  particular, when K is 1 and 'Use Merge Credits' is true, the cluster
  L may contain only as many 'small' schemes as there are 'large' 
  schemes in L. 

  The purpose of this parameter is to prevent a scenario in which one 
  large scheme (supported by more than N unique types) merges with a 
  small scheme to form a new large(r) cluster, L'. Per the 'At least 
  one child must cover >N Types' parameter, the L' cluster could then 
  mop up a second small scheme, forming a cluster L''. But L'' could 
  then mop up a third small scheme. Etc. ad nausium. 

<SetTITCF> : <Set> <T>ie <i>n <T>ype <C>overed <F>ilter : 
  Default: 'true' : If set to 'true', then, at the end of clustering, 
  ParaMor will discard **all** scheme clusters which are supported by 
  fewer unique types than the value of the 'At least one child must 
  cover >N Types' parameter. This parameter effectively ties the 'At 
  least one child must cover >N Types' parameter to the 'types covered
  cluster filtering' parameter that is discussed in Section 2.3.4.1.3.
  
  When the 'At least one child must cover >N Types' parameter is at 
  its default value (37), when 'Use merge Credits' is 'true', and when 
  this 'Tie in Type Covered Filter' is 'true', the effect is to 
  discard all the small schemes that didn't merit inclusion in some 
  larger cluster. (A few larger singleton scheme-clusters may survive.
  A singleton scheme-cluster, L,  will survive when L is never merged 
  with any other cluster, but L alone has more than 'N' supporting 
  word types.)
  
Once you are satisfied with the parameter settings that ParaMor should
use for clustering, you may begin the actual clustering procedure by
invoking the <C>luster command:

<C> : <C>luster : After confirming with 'y' that you wish to cluster 
with the current setting of parameters, ParaMor prints progress 
information to the screen as the clustering algorithm proceeds. 
ParaMor prints three phases of progress information. First, as ParaMor
wraps each original selected scheme into a (degenerate singleton) 
cluster, ParaMor prints 'Initialized X clusters of Y'. Second, ParaMor
computes the pairwise similarities of each pair of clusters. During 
this second phase ParaMor prints 'Initialized N merges of M'. Third 
and finally, ParaMor iteratively merges the most similar pair of 
clusters until ParaMor's halting criteria are met. During this third
phase, ParaMor periodically prints the number of clusters that remain 
to be considered for clustering.

When clustering finishes, ParaMor prompts the user to press [Enter],
which will then dump you out at the 'Search and Processing Menu'.


        2.3.4.1.3 Types Covered Filter
       - - - - - - - - - - - - - - - - -

Executing the <TCFC> command from the 'Search and Processing Menu' 
(Section 2.3.4.1) takes you to the 'Types Covered Filter'. From 
here you can instruct ParaMor to discard clusters that are supported
by fewer than a specified threshold number of types. A word type, Y,
supports a cluster, L, if for some scheme, C in L, there exists a 
stem, T in C, and a suffix, F in C, such that T.F = Y.

The 'Types Covered Filter' discards paradigm clusters that, because of
their small size are unlikely to model true paradigms of a language.
Discarding unlikely candidate paradigm clusters is exactly Task E as 
described in Section 1. While 'small' clusters are one type of 
unlikely paradigm candidate, two other types are discussed in 
Section 2.3.4.1.4.

PLEASE NOTE: If the 'tie in type covered filter' parameter was set to
'true' on the 'Clustering Menu', then:

  YOU DO NOT NEED TO EXECUTE THE <TCFC> COMMAND

because ParaMor will have already discarded clusters that are 
supported by fewer than N types, where N is the value of the 'At least
one child must cover >N Types' parameter, also from 'Clustering Menu'.

Also note that you may only call the <TCFC> command after you have 
performed the <C>lustering command from the 'Search and Processing 
Menu'.

The 'Types Covered Filter' menu is quite simple with just four 
possible commands to run:

<SetL> : <Set> Leaf average <L>evel must be N or Less to Filter :
  Default: 'false' : Deprecated, this parameter was a research 
  direction that didn't pan out. Don't set this parameter to 'true'.

<SetCTC> : <Set> <C>overed <T>ypes <C>utoff parameter to search over :
  Default: [37] : Call this command to set the size threshold--ParaMor
  will discard all types that are covered by fewer types than the 
  value of this parameter.

<F>ilter : Perform ParaMor's Type Covered Filter on Clusters. 
  Filtering clusters for covered types is very fast, a few seconds 
  long at the outside.

<R>eturn to the Search and Processing Menu.


        2.3.4.1.4 Morpheme Boundary Too Far Left/Right Filters
       - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

Executing the <MBTFLFC> or the <MBTFRFC> commands from the 'Search and
Processing Menu' (Section 2.3.4.1) takes you to the 'Morpheme Boundary
Too Far Left Filter' and the 'Morpheme Boundary Too Far Right Filter'
respectively. These filters discard paradigm clusters whose 
hypothesized morpheme boundaries are likely misplaced internal to true 
morphemes. 

ParaMor searches for candidate paradigms that misplace their 
boundaries by, in the case of the 'Too Far Left Filter', examining the
distribution of characters that *begin* the candidate *suffixes* of a 
cluster and, in the case of the 'Too Far Right Filter', examining the 
distribution of characters that *end* the candidate *stems* of a 
cluster. A character distribution that places the vast majority of its 
probability mass on just one or two characters is indicative of a 
hypothesized boundary that actually lies inside a true morpheme, while
a more uniform character distribution is typical of true morpheme 
boundaries (see Harris, 1955; Haffer and Weiss, 1974; and Goldsmith, 
2001). The intuition is that a wide variety of morphemes (and hence a 
wide variety of characters) are allowed to follow (and similarly to 
precede) a morpheme boundary, but when you are inside a morpheme (or 
inside a group of morphemes in the case of a candidate paradigm) the 
next (or previous) character is constrained to be the next (or 
previous) character of the current morpheme (or group of morphemes).
Although the intuition is straightforward, the detailed implementation
is somewhat involved. For a full description of both the 'Too Far 
Left' and the 'Too Far Right' filters, see Section 4.4.2 of 
Monson (2009).

The 'Too Far Left' and 'Too Far Right' Filter menus are very simple:
you can select a metric to use for measuring character variation, you 
can set a cutoff value over the selected metric, you can run the 
filter itself, and you can return to the 'Search and Processing' menu.
Here are the commands in detail:

<SetLM> : <Set> <L>eft-looking <M>etric : Default: ENTROPY : This is
  the metric that ParaMor uses to measure the variation within 
  specific distributions of stem-final characters. (Note that for 
  reasons I did not describe here, both the 'Too Far Right' and, 
  unexpectedly, the 'Too Far Left' filter ultimately examine sets of 
  *stem-final* characters (see Monson, 2009). Hence, both the 'Too Far
  Left' and the 'Too Far Right' menus have <SetLM> and <SetLMC> 
  commands and not <SetRM> or <SetRMC> commands). ParaMor
  allows you to chose from a few different metrics of variation, but I
  recommend sticking with entropy--as Haffer and Weis (1974) show, 
  entropy is more robust than most other metrics at this task.

<SetLMC> : <Set> <L>eft-looking <M>etric <C>utoff parameters to search
  over : Default [0.5] : This parameter holds a cutoff value that 
  defines the value of the 'Left-looking Metric' that ParaMor takes as
  marking a true morpheme boundary. In the case of entropy, low 
  entropy means little character variation which in turn suggests a
  hypothesized boundary that lies internal to a true morpheme, while 
  high entropy means a more uniform distribution and therefore a 
  likely true morpheme boundary. Consequently, when the 'Left-looking
  Metric' is entropy, ParaMor will discard paradigm clusters whose
  leaf schemes have a low average entropy.

  At the default of 0.5, ParaMor will discard scheme clusters whose 
  average stem-final character entropy is less than 0.5. An entropy of
  0.5 is actually quite low, and so the default will essentially only 
  discard scheme clusters where virtually all of the candidate stems 
  end in the same character. For a full explanation for why 
  conservative filtering works well here, see Monson (2009).

<F>ilter : Run the actual 'Too Far Left' or 'Too Far Right' filtering
  algorithm.

<R>eturn : Takes the user back to the 'Search and Processing Menu'.


        2.3.4.1.5 Segmentation Menu
       - - - - - - - - - - - - - - -

Executing the <Seg> command from the 'Search and Processing Menu' 
(Section 2.3.4.1) takes you to the 'Segmentation Menu' which will
allow ParaMor to morphologically segment a set of words. Morphological
segmentation is Task F (the final task) of ParaMor from Section 1.

Aside from a command to <R>eturn to the 'Search and Processing Menu',
there are four main commands on the 'Segmentation Menu', two of which
segment words, and two of which write out a set of segmented words to
a file. When ParaMor v0.1 segments a set of words, ParaMor saves the
segmentations in memory but does not immediately write out the 
segmentations in a human-readable format--to actually see the 
segmentations you must invoke one of the writing commands.


Segmentation Commands:

<SegCor> : <Seg>ment all the words in the <Cor>pus : This command 
  directly segments the set of words that was used to build the
  candidate paradigms (i.e. the words that you read in during Task A
  at the 'Corpus Menu' that was described in Section 2.3.2). Once 
  ParaMor has finished segmenting the words of the input corpus, 
  ParaMor will automatically return to the 'Segmentation Menu'.

<SegFile> : <Seg>ment all the words in some <File> : This command will
  allow you to use ParaMor's discovered paradigms to segment some
  arbitrary set of words. ParaMor's bottom-up scheme search and 
  paradigm clustering algorithms are time and memory intensive and so
  you may only be able to build paradigms over a subset of the data
  that you would ultimately like to segment. In the 2007/2008/2009 
  Morpho Challenge competitions, for example, I built paradigms over 
  the 50,000 most frequent word types for each language scenario, but 
  then used the <SegFile> command to segment the full Morpho Challenge
  corpora. The full Challenge corpora contain hundreds of thousands, 
  or in some cases millions, of unique word types.

  The <SegFile> command works by reusing the 'Corpus Menu' command 
  that was described in Section 2.3.2. As detailed in Section 2.3.2, 
  the 'Corpus Menu' allows ParaMor to read in a corpus according to a 
  variety of considerations: ParaMor can preserve or ignore case, can 
  retain or discard numbers, retain or discard short word types, etc. 
  By reusing the 'Corpus Menu' system at segmentation time, it is 
  possible to instruct ParaMor to read in exactly the same set of 
  tokens for segmentation that were used during paradigm induction. 
  But more interestingly, reusing the 'Corpus Menu' allows ParaMor to
  segment a set of words that differs from the induction corpus in 
  some crucial way. A typical case would be to segment a corpus with
  the same casing that was used during paradigm induction but that 
  retains short types that were not used for paradigm induction.

  In fact, by default the 'Corpus Menu' command's parameters are set
  at the values that were used to complete ParaMor's Task A: Read in a
  Corpus--with two crucial exceptions. First, this time around ParaMor
  is set to read in *all* of the words of the input file not some 
  limited number of types like 50,000. Second, ParaMor will include
  all type lengths, both long and *short*, as it reads in types. 
  Still, if you wish to change any of ParaMor's corpus-read-in 
  parameters, you may do so at this time.

  IMPORTANT: Once the parameters that govern corpus read-in are set at
  the values you desire, call the <RC> command to actually read in and
  segment a set of words. 

  ParaMor prints a variety of progress information as it segments a 
  set of words. Most helpful are the progress lines which explicitly 
  state the number of words that have bee segmented thus far. But 
  ParaMor's segmentation algorithm will also build many small scheme 
  networks (See Section 2.3.3) and will print progress information 
  relating to the creation of these networks.
  

Commands that Write Out Segmentations:

<WS> : <W>rite <S>egmentation : This command writes out, in a 
  human-readable format, ParaMor's morphological segmentations of the
  words that were segmented by either the <SegCor> or <SegFile> 
  commands. The human-readable format that is written is exactly the 
  format used in the Morpho Challenge 2007/2008/2009 competitions. 
  Specifically, each segmented word appears on a separate line, and 
  each line has the form:

  <SurfaceWord><Tab><Segmentation#1><comma><Segmentation#2>...

  While a <Segmentation> has the form:

  <morpheme>[<space><morpheme>]*

  And each <morpheme> may be optionally marked as an affix by 
  prepending a '+' character.

  For example, here are some lines that might occur in an output file
  of segmented words:

catch	catch
dances	dance +s
dancing	danc +ing 
flies	fly_N +PL, fly_V +3sg
  
  When you invoke the <WS> command, ParaMor leads you though a series
  of menus. First, ParaMor asks you to 'Please select which 
  segmentations in what format to print'. Unless you are doing tricky
  things, you want to select option [3] 'COMBINED_SEGMENTATION'. The 
  'COMBINED_SEGMENTATION' mode outputs a single segmentation for each
  word, where that single segmented form can contain more than one 
  morpheme boundary. 'COMBINED_SEGMENTATION' is the mode that I used 
  for the 2008 and 2009 Challenges. (In the 2007 Challenge, I used 
  mode [0] 'ALL_SEGMENATIONS_AS_SEPARATE_ANALYSES', which outputs 
  several comma-separated morphological analyses for each word, where 
  each analysis contains at most a single morpheme boundary. The other
  segmentation modes, [1] and [2], were invented for particular 
  experiments during the development of ParaMor).

  The second menu that ParaMor leads you to, after calling the <WS>
  command, asks you to 'Enter the fully qualified file name **prefix**
  to write the segmentation results and explanations to'. For example,
  if you want to write ParaMor's segmentations to a file in the /home
  directory, and if you want the name of the file of segmentations to
  begin with the string 'trial1', then you would type: '/home/trial1'
  at the prompt. 

  Once you have entered a filename prefix, ParaMor will present to you
  the full filename where ParaMor will write the segmentations. To 
  form the full filename, ParaMor concatenates onto the filename 
  prefix the values of the more pertinent parameters that ParaMor used
  during search, clustering, and filtering of paradigms. To accept the
  full name, which may be quite long, type 'y' at the prompt.

  The final menu that the <WS> command will invoke will ask you 
  whether you would like the segmentation file to be written out using
  the 'utf-8' or the 'latin-1' encoding. The utf-8 encoding is a 
  particular encoding of the Unicode character set. Unicode can 
  represent just about any character that exists in just about all of 
  the world's writing scripts. So if you are segmenting text from some
  non-western-European language, you may need to print out ParaMor's 
  segmentations using utf-8. 
  
  Unfortunately, if you would like to use the evaluation methodology 
  of Morpho Challenge, you must represent your segmentations in a 
  character encoding that uses at most 8 bits to represent any single 
  character. Despite its misleading name, the utf-8 encoding often 
  uses more than 8-bits to represent characters. Latin-1, the other 
  encoding option that ParaMor suggests, *does* use just 8 bits for 
  each character. However, latin-1 can only represent characters that 
  occur in a subset of western European languages, including all the 
  characters from such languages as English, Spanish, German, and 
  Italian.

  After selecting the character encoding, ParaMor will directly write
  out the segmented file, and lead you back to the 'Segmentation 
  Menu'.

<WSE> : <W>rite <S>egmentation <E>xplanation : This command will write
  out a human readable synopsis of *why* ParaMor proposed each 
  particular morpheme boundary. Take a word, w, that ParaMor 
  segments. Each morpheme boundary, b, that ParaMor inserts into w, 
  segments w into a stem, t, and a suffix, f, i.e. w = t.f . For each
  b in each w, ParaMor's <WSE> command prints:

  1) w
  2) w segmented at b
  3) A suffix f' such that f'!=f and t.f' is a word that occurred in 
     the corpus
  4) A paradigm cluster, c, (i.e. a set of candidate suffixes) that 
     has as members both f and f'.

  For example, after segmenting one corpus of Spanish, ParaMor gave
  the following explanations for two of its segmentations:

----------------------------
conectada

	conectad +a
		+a --> (os)  	(a as o os)

----------------------------
conectadas

	conectad +as
		+as --> (os)  	(a as o os)

	conectada +s
		+s --> (*null*)  	(*null* ndo ron s)

----------------------------

  For the word 'conectada', ParaMor proposed a single morpheme 
  boundary just before the final 'a'. This boundary was proposed 
  because the suffix 'os' is substitutable for 'a' (i.e. the word 
  'conectados' occurred in the corpus) and because the '(a as o os)' 
  paradigm contains both the suffix 'a' and the suffix 'os'.
 
  Similarly, in the word 'conectadas', ParaMor proposed two morpheme 
  boundaries: one before the word-final string 'as', and one before 
  the word-final 's'. These two morpheme boundaries are justified 
  because 'os', from the paradigm '(a as o os)', can substitute for 
  'as', while '*null*' can substitute for 's' from the 
  '(*null* ndo ron s)' paradigm (that is, 'conectados' and 'conectada'
  each occurred in this corpus of Spanish in addition to the original
  word, 'conectadas'). Note that the final segmentation that ParaMor 
  outputs for the word 'conectadas' would be: conectad +a +s.

  When you invoke the <WSE> command, ParaMor leads you through a 
  series of menus that is similar to the menu series that follows the
  <WS> command. ParaMor asks for a filename (prefix) to write the
  segmentation explanations to, and asks you what encoding you would
  like the explanation file written in. See the entry for the <WS>
  command, in this same Sub-Section, for a more detailed explanation
  of these menus.


--------------------
 3. Command Summary
--------------------

A typcial run of ParaMor requires the user to enter quite a sequence
of menu commands. Here is a summary of the commands you will need to
call in a basic run of ParaMor. The following assumes that the 
initializations file already contains the name of the text file that 
you want to process:

1) <SC> from Root Menu
2) <RC> from Corpus Menu
3) <CN> from Root Menu
4) <CN> from Create Network Menu
5) <SP> from Root Menu
6) <BUS> from Search and Processing Menu
7) <S> from Bottom Up Search Menu
   <y> at confirmation prompt
   <Enter> to conclude search
8) <C> from Search and Processing Menu
9) <C> from Clustering Menu
   <y> at confirmation prompt
   <Enter> to conclude clustering
10) <MBTFLFC> from Search and Processing Menu
11) <F> from Morpheme Boundary Too Far Left Filter Menu
    <y> at confirmation prompt
    <Enter> to conclude too-far-left fileter
12) <MBTFRFC> from Search and Processing Menu
13) <F> from Morpheme Boundary Too Far Right Filter Menu
    <y> at confirmation prompt
    <Enter> to conclude too-far-right filter
14) <SEG> from Search and Processing Menu
15) <SegFile> from Segmentation Menu
16) <RC> from Corpus Menu
17) <WS> from Segmentation Menu
    <3> to select COMBINED_SEGMENTATION
    <output filename> at prompt
    <y> at confirmation prompt
    <1> to select output in utf-8


----------------
 4. Conclusions
----------------

And that's it! You now know how to run ParaMor. 

If you have questions about running ParaMor please contact me, 
Christian Monson, at: monsonc@csee.ogi.edu. I will do my best to 
answer your questions, with the caveat that ParaMor, as a research
system, will likely have its flaws.

Good Luck!

Christian Monson


------------
 References
------------

Goldsmith, John. 2001. Unsupervised Learning of the Morphology of a 
    Natural Language. Computational Linguistics, 27.2, 153-198.

Hafer, Margaret A., and Stephen F. Weiss. 1974. Word Segmentation by 
    Letter Successor Varieties. Information Storage and Retrieval, 
    10.11/12, 371-385.

Harris, Zellig. 1955. From Phoneme to Morpheme. Language, 31.2, 
    190-222, Reprinted in Harris, Zellig. 1970. Papers in Structural 
    and Transformational Linguists. Ed. D. Reidel, Dordrecht.

Monson, Christian. 2009.  ParaMor: From Paradigm Structure to Natural 
    Language Morphology Induction. Ph.D. Thesis, Language Technologies
    Institute, School of Computer Science, Carnegie Mellon University.
