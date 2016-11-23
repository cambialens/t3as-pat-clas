# Patent Classification API

This API provides endpoints to search and retrieve classification related data. This document currently covers CPC data but the other (deprecated) classification schemes (USPC and IPC) are implemented in a similar manner.
 
An explanation of the CPC classification scheme can be found at Wikipedia: https://en.wikipedia.org/wiki/Cooperative_Patent_Classification


## Getting Data for classification symbols

### Get the data for a classification symbol and its ancestors (single symbol, see multiple below)
 
#### Request 
 
Method: GET 

Request Params:
* symbol = classification symbol (code)
* result format = text|xml


#### Response

Content-Type: application/json

An array of data containing an element for each symbol in the hierarchy: 
* id : the ID of the classification symbol
* symbol : the classification symbol
* level : the level of the classification in the official hierarchy. Higher values are further from the root. Note that for CPC, levels 3 and 6 will be missing, but the level ordering from high to low is still consistent.  
* classTitle : The title for the classification.
* notesAndWarnings : Notes about how the classification is applied.


#### Example:

http://dev.lens.org/patclass/rest/v1.0/CPC/ancestorsAndSelf?symbol=F24B13/04&format=text

Response:

```
[

  {
    "id": 506,
    "symbol": "F",
    "level": 2,
    "classTitle": "MECHANICAL ENGINEERING\nLIGHTING\nHEATING\nWEAPONS\nBLASTING ENGINES OR PUMPS",
    "notesAndWarnings": "Guide to the use of this subsection (classes\nF01\nto\nF04\n)\nThe following notes are meant to assist in the use of this part of the classification scheme.\nIn this subsection, subclasses or groups designating \"engines\" or \"pumps\"\ncover\nmethods of operating the same, unless otherwise specifically provided for.\nIn this subsection, the following terms or expressions are used with the meanings indicated:\n- \"engine\" means a device for continuously converting fluid\nenergy into mechanical power. Thus this term includes, for\nexample, steam piston engines or steam turbines, PER SE, or\ninternal-combustion piston engines, but it excludes single-\nstroke devices. \"Engine\" also includes the fluid-motive\nportion of a meter unless such portion is particularly\nadapted for use in a meter;\n- \"pump\" means a device for continuously raising, forcing,\ncompressing, or exhausting fluid by mechanical or other\nmeans; thus this term includes fans or blowers;\n- \"machine\" means a device which could equally be an engine\nand a pump, and not a device which is restricted to an\nengine or one which is restricted to a pump;\n- \"positive displacement\" means the way the energy of a\nworking fluid is transformed into mechanical energy, in\nwhich variations of volume created by the working fluid in\na working chamber produce equivalent displacements of the\nmechanical member transmitting the energy, the dynamic\neffect of the fluid being of minor importance; and VICE-\nVERSA;\n- \"non-positive displacement\" means the way the energy of a\nworking fluid is transformed into mechanical energy, by\ntransformation of the energy of the working fluid into\nkinetic energy; and VICE-VERSA;\n- \"oscillating-piston machine\" means a positive-displacement\nmachine in which a fluid-engaging work-transmitting member\noscillates. This definition applies also to engines and\npumps;\n- \"rotary-piston machine\" means a positive-displacement\nmachine in which a fluid-engaging work-transmitting member\nrotates about a fixed axis or about an axis moving along a\ncircular or similar orbit. This definition applies also to\nengines and pumps;\n- \"rotary piston\" means the work-transmitting member of a\nrotary-piston machine and may be of any suitable form, e.g.\nlike a toothed gear;\n- \"co-operating members\" means the \"oscillating piston\" or\n\"rotary piston\" and another member, e.g. the working-chamber\nwall, which assists in the driving or pumping action;\n- \"movement of the co-operating members\" is to be interpreted\nas relative, so that one of the \"co-operating members\" may\nbe stationary, even though reference may be made to its\nrotational axis, or both may move;\n- \"teeth or tooth-equivalents\", include lobes, projections or\nabutments;\n- \"internal-axis type\" means that the rotational axes of the\ninner and outer co-operating members remain at all times\nwithin the outer member, e.g. in a similar manner to that of\na pinion meshing with the internal teeth of a ring gear;\n- \"free-piston\" means a piston of which the length of stroke\nis not defined by any member driven thereby;\n- \"cylinders\" means positive-displacement working chambers in\ngeneral and thus this term is not restricted to cylinders\nof circular cross-section;\n- \"main shaft\" means the shaft which converts reciprocating\npiston motion into rotary motion or VICE-VERSA;\n- \"plant\" means an engine together with such additional appa-\nratus as is necessary to run the engine. For example, a\nsteam engine plant includes a steam engine and means for\ngenerating the steam;\n- \"working fluid\" means the driven fluid in a pump and the\ndriving fluid in an engine. The working fluid may be in a\ngaseous state, i.e. compressible, or liquid. In the former\ncase coexistence of two states is possible;\n- \"steam\" includes condensable vapours in general, and\n\"special vapour\" is used when steam is excluded;\n- \"reaction type\" as applied to non-positive-displacement\nmachines or engines means machines or engines in which\npressure/velocity transformation takes place wholly or\npartly in the rotor; machines or engines with no, or only\nslight, pressure/velocity transformation in the rotor are\ncalled \"impulse type\".\nIn this subsection:\n- cyclically operating valves, lubricating, gas-flow silencers\nor exhaust apparatus, or cooling should be classified in\nsubclasses\nF01L\n,\nF01M\n,\nF01N\n,\nF01P\nirrespective of their\nstated application, unless their classifying features are\npeculiar to their application, in which case they should be\nclassified only in the relevant subclass of classes\nF01\nto\nF04\n;\n- lubricating, gas-flow silencers or exhaust apparatus, or\ncooling of machines or engines should be classified in sub-\nclasses\nF01M\n,\nF01N\n,\nF01P\nexcept for those peculiar to steam\nengines which should be classified in subclass\nF01B\n.\nFor use of this subsection with a good understanding, it is essential to remember, so far as subclasses\nF01B\n,\nF01C\n,\nF01D\n,\nF03B\n,\nF04B\n,\nF04C\nand\nF04D\n, which form its skeleton, are concerned:\n- the principle which resides in their elaboration\n- the classifying characteristics which they call for, and\n- their complementarity\nPrinciple\nThis concerns essentially the subclasses listed above. Other subclasses, notably those of class\nF02\n, which cover better-defined matter, are not considered here.Each subclass covers fundamentally a genus of apparatus (engine or pump) and by extension covers equally \"machines\" of the same kind. Two different subjects, one having a more general character than the other, are thus covered by in the same subclassSubclasses\nF01B\n,\nF03B\n,\nF04B\n, beyond the two subjects which they cover, have further a character of generality in relation to other subclasses concerning the different species of apparatus in the genus concerned. This generality applies as well for the two subjects dealt with, without these always being in relation to the same subclasses.Thus, subclass\nF03B\n, in its part dealing with \"machines\" should be considered as being the general class relating to subclasses\nF04B\n,\nF04C\nand in its part dealing with \"engines\" as being general in relation to subclass\nF03C\n.\nCharacteristics\nThe principal classifying characteristic of the subclass is that of genera of apparatus, of which there are three possible:\nMachines; engines; pumps.\nAs stated above, \"machines\" are always associated with one of the other two genera.These main genera are subdivided according to the general principles of operation of the apparatus:\nPositive displacement; non-positive displacement.\nThe positive displacement apparatus are further subdivided according to the ways of putting into effect the principle of operation, that is, to the kind of apparatus:\nSimple reciprocating piston; rotary or oscillating\npiston; other kind.\nAnother classifying characteristic is that of the working fluid, in respect of which three kinds of apparatus are possible, namely:\nLiquid and elastic fluid; elastic fluid; liquid.\nComplementarity\nThis resides in association of pairs of the subclasses listed above, according to the characteristics under consideration in respect of kind of apparatus or working fluid.\nThe subclasses concerned with the various principles, characteristics and complementarity are shown in the following table:\n______________________________________________________________________\nKind positive non- Working fluid Relations\nof ____________________ posi- ______________________ of gene-\ndis- rotary tive rality in\nplace- reci- or os- liquid respect\nment pro- cillat- and of kind\ncating ing elastic elastic of dis-\npiston piston other fluid fluid liquid placement\n______________________________________________________________________\nMACHINES\n__________\nX X X X\nF01B\nX X X\nF01C\nX X X\nF01D\nX X\nF03B\nX X X\nF04B\nX X\nF04C\nENGINES\n__________\nX X X X\nF01B\nX X X\nF01C\nX X X\nF01D\nX X\nF03B\nX X X X\nF03C\nPUMPS\n__________\nX X X X X\nF04B\nX X X X\nF04C\nX X X X\nF04D\n______________________________________________________________________\nIt is seen from the table that :\n- For the same kind of apparatus in a given genus, the\ncharacteristic of \"working fluid\" associates:\nF01B\nand\nF04B\n)\nF01C\nand\nF04C\n) Machines\nF01D\nand\nF03B\n)\nF01B\nand\nF03C\n)\nF01C\nand\nF03C\n) Engines\nF01D\nand\nF03B\n)\n- For the same kind of working fluid, the \"apparatus\"\ncharacteristic relates subclasses in the same way as\nconsiderations of relative generality."
  },
  {
    "id": 590,
    "symbol": "F24",
    "level": 4,
    "classTitle": "HEATING\nRANGES\nVENTILATING\nprotecting plants by heating in gardens, orchards, or forests\nA01G13/06\n; baking ovens and apparatus\nA21B\n; cooking devices other than ranges\nA47J\n; forging\nB21J\n,\nB21K\n; specially adapted for vehicles, see the relevant subclasses of\nB60\nto\nB64\n; combustion apparatus in general\nF23\n; drying\nF26B\n; ovens in general\nF27\n; electric heating elements and arrangements\nH05B",
    "notesAndWarnings": "In this class, the following terms are used with the meanings indicated:\n- \"stove\" includes apparatus which may have an open fire, e.g.\nfireplace;\n- \"range\" means an apparatus for cooking having elements that\nperform different cooking operations or cooking and heating\noperations."
  },
  {
    "id": 591,
    "symbol": "F24B",
    "level": 5,
    "classTitle": "DOMESTIC STOVES OR RANGES FOR SOLID FUELS",
    "notesAndWarnings": ""
  },
  {
    "id": 170616,
    "symbol": "F24B13/00",
    "level": 7,
    "classTitle": "Details solely applicable to stoves or ranges burning solid fuels\ncomponent parts or accessories for stoves with open-fires\nF24B1/191\n; removing ash, clinker or slag from combustion chambers\nF23J1/00\n; removing solid residues from passages or chambers beyond the fire\nF23J3/00\n; joints or connections for chimneys or flues\nF23J13/04\n; mouths or inlet holes for chimneys or flues\nF23J13/06\n; means for supervising combustion\nF23M11/04",
    "notesAndWarnings": ""
  },
  {
    "id": 170622,
    "symbol": "F24B13/04",
    "level": 8,
    "classTitle": "Arrangements for feeding solid fuel, e.g. hoppers\nfeeding solid fuel to combustion apparatus in general\nF23K",
    "notesAndWarnings": ""
  }
]

```


### Get the data for multiple classification symbols and their ancestors

#### Request

Method: POST 

Request Body:
* symbols = array of classification symbols (codes)
* result format = text|xml

#### Response

Content-Type: application/json

Map of request symbol to the data for that symbol and it's ancestors. Data returned for each symbol is the same as for single request above.
* id : the ID of the classification symbol
* symbol : the classification symbol
* level : the level of the classification in the official hierarchy. Higher values are further from the root. Nnote that for CPC, levels 3 and 6 will be missing, but the level ordering from high to low is still consistent.  
* classTitle : The title for the classification.
* notesAndWarnings : Notes about how the classification is applied.


#### Example 

http://dev.lens.org/patclass/rest/v1.0/CPC/bulkAncestorsAndSelf

```
{
	"symbols" : ["A", "B"],
	"format" : "text"
}
```

Response:

eg: http://dev.lens.org/patclass/rest/v1.0/CPC/ancestorsAndSelf?symbol=F24B13/04&format=text

```
{
  "A": [
    {
      "id": 1,
      "symbol": "A",
      "level": 2,
      "classTitle": "HUMAN NECESSITIES",
      "notesAndWarnings": ""
    }
  ],
  "B": [
    {
      "id": 101,
      "symbol": "B",
      "level": 2,
      "classTitle": "PERFORMING OPERATIONS\nTRANSPORTING",
      "notesAndWarnings": "The following notes are meant to assist in the use of\nclasses\nB01\nto\nB09\n; they must not be read as modifying in any way the elaborations.\nIn this sub-section, the separation of different materials, e.g. of different matter, size, or state, is predominantly found in the following subclasses:\nB01D\nB03B\n,\nB03C\n,\nB03D\nB04B\n,\nB04C\nB07B\n,\nB07C\nThe classifying characteristics of these subclasses are:\n- the physical state of the matter to be separated\n- the principle of the process used\n- particular kinds of apparatus\nThe first of these characteristics involves six different\naspects, assembled in three groups.\na liquid/liquid or liquid/gas and gas/gas\nb solid/liquid or solid/gas\nc solid/solid\nTheses subclasses are to be used according to the following general rules:\n-\nB01D\nis the most general class as far as separation other\nthan solids from solids is concerned.\n- Apparatus for separating solids from solids are covered by\nB03B\nwhen the process concerned is regarded as the equivalent\nof \"washing\" in the sense of the mining art, even if such\napparatus is a pneumatic one, especially pneumatic tables or\njigs. Screens PER SE are not covered by this subclass but\nare classified in\nB07B\n, even if they are being used in a\nwet process. All other apparatus for the separation of solids\nfrom solids according to dry methods are classified in\nB07B\n.\n- If the separation takes place as a result of the detection\nor measurement of some feature of the material or articles to\nbe sorted it is classified in\nB07C\n.\nIt should also be noted that the separation of isotopes of the\nsame chemical element is covered by\nB01D59/00\n, whatever\nprocess or apparatus is employed.\n4. The following scheme illustrates the classification according\nto these rules.\n(a) LIQUID/LIQUID\nLIQUID/GAS\nGAS/GAS\n_____________________________________________________________________\nSubclasses dealing with\nOperations Method Apparatus\n_____________________________________________________________________\nGeneral\nB01D\nB01D\nby centrifugal force, using centrifuges\nor free-vortex apparatus\nB01D\nB04B\n,\nB04C\nusing magnetic or electrostatic effect\nB03C\nB03C\n_____________________________________________________________________\n(b) SOLID/LIQUID\nSOLID/GAS\n_____________________________________________________________________\nSubclasses dealing with\nOperations Method Apparatus\n_____________________________________________________________________\nGeneral\nB01D\nB01D\nby centrifugal force\nB01D\nB01D\nusing centrifuges or free-vortex\napparatus\nB01D\nB04B\n,\nB04C\nusing magnetic or electrostatic effect\nB03C\nB03C\n_____________________________________________________________________\n(c) SOLID/SOLID\n_____________________________________________________________________\nDry Methods\n_____________________________________________________________________\nSubclasses dealing with\nOperations Method Apparatus\n_____________________________________________________________________\nGeneral for material in bulk\nB07B\nB07B\nIndividual sorting\nB07C\nB07C\nScreening, sifting, pneumatic sorting\nB07B\nB07B\nusing pneumatic tables or jigs\nB03B\nB03B\nby magnetic or electrostatic effect\nB03C\nB03C\nby centrifugal force\nB07B\nB07B\nusing centrifuges or free-vortex\napparatus\nB07B\nB04B\n,\nB04C\n_____________________________________________________________________\nWet Methods\n_____________________________________________________________________\nSubclasses dealing with\nOperations Method Apparatus\n_____________________________________________________________________\nGeneral\nB03B\nB03B\nflotation, differential sedimentation\nB03D\nB03D\nscreening\nB07B\nB07B\n_____________________________________________________________________\nCombinations = dry methods - wet methods:\nB03B\n_____________________________________________________________________"
    }
  ]
}
```

## Searching for classification symbols

### Auto complete of words found within classification symbols and their text content

#### Request

Method: GET 

Request Body:
* prefix = The prefix of the string to complete
* num = The number of results to return

#### Response

Content-Type: application/json

An array of 'exact' and and an array of 'fuzzy' string matches for the prefix. TODO: what does 'fuzzy' match actually match?

#### Example

Request:

http://dev.lens.org/patclass/rest/v1.0/CPC/suggest?prefix=foo&num=5


Response:

```
{
  "exact": [
    "<b>foo</b>t",
    "<b>foo</b>d",
    "<b>foo</b>twear",
    "<b>foo</b>dstuffs",
    "<b>foo</b>tball"
  ],
  "fuzzy": []
}
```


### Search for classification symbols based on their text content

#### Request:

Method: GET

* q : the search string to search classification text for (lucene search query language), available fields are:
  ** ???
  ** ???
* prefix : prefix of a symbol to restrict the search to - default is no prefix restriction
* stem : should search strings be stemmed or not - default true. Eg a search for 'locomotives' would match 'locomotive' if stemming is true (default) 'stemmed'

#### Response

Content-Type: application/json

A list of search results containing data for the matching classifications:

* score : result score
* symbol : raw and formatted versions of the matched symbol. Use the formatted version. TODO Do these ever differ?
* level : the level of the classification in the official hierarchy. Higher values are further from the root. Nnote that for CPC, levels 3 and 6 will be missing, but the level ordering from high to low is still consistent.        
* classTitle : The title for the classification.
* notesAndWarnings : Notes about how the classification is applied.


Map of request symbol to the data for that symbol and it's ancestors. Data returned for each symbol is the same as for single request above.
 
#### Example

Request:

http://dev.lens.org/patclass/rest/v1.0/CPC/search?q=locomotive&stem=true&symbol=F22G7

Response:

```
[
  {
    "score": 3.4648771,
    "symbol": {
      "raw": "F22G7/065",
      "formatted": "F22G7/065"
    },
    "level": 9,
    "classTitle": "for <span class=\"hlight\">locomotive</span> boilers",
    "notesAndWarnings": ""
  },
  {
    "score": 3.4648771,
    "symbol": {
      "raw": "F22G7/105",
      "formatted": "F22G7/105"
    },
    "level": 9,
    "classTitle": "for <span class=\"hlight\">locomotive</span> boilers",
    "notesAndWarnings": ""
  },
  {
    "score": 1.7324386,
    "symbol": {
      "raw": "F22G7/005",
      "formatted": "F22G7/005"
    },
    "level": 8,
    "classTitle": "for <span class=\"hlight\">locomotive</span> boilers\nF22G7/065\n,\nF22G7/105\ntake precedence",
    "notesAndWarnings": ""
  }
]
```



```
{
  "exact": [
    "<b>foo</b>t",
    "<b>foo</b>d",
    "<b>foo</b>twear",
    "<b>foo</b>dstuffs",
    "<b>foo</b>tball"
  ],
  "fuzzy": []
}
```