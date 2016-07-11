grammar DockerImageName;

imageName    :  (repository SLASH)? (namespace SLASH)? name (DCOLON tag)?  CRLF;

repository   :  (LETTER|NUMBER|SPECIAL)+ DCOLON NUMBER+;

namespace    :  (LETTER|NUMBER|SPECIAL)+;

name         :  (LETTER|NUMBER|SPECIAL)+;

tag          :  (LETTER|NUMBER|SPECIAL)+;

DCOLON       :   ':';

SLASH        :   '/';

LETTER       :   [a-zA-Z];

NUMBER       :   [0-9];

SPECIAL      :   ('_'|'-'|'.');

CRLF         :   ('\r\n'|'\n'|'\r');
