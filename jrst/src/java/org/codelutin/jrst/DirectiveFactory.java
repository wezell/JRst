/* *##%
 * Copyright (C) 2002, 2004 Code Lutin
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *##%*/

/* *
 * DirectiveFactory.java
 *
 * Created: 23 juin. 2004
 *
 * @author Bucas
 * Copyright Code Lutin
 * @version $Revision$
 *
 * Mise a jour: $Date$
 * par : $Author$
 */

package org.codelutin.jrst;

public class DirectiveFactory extends AbstractFactory { // DirectiveFactory

    /** les diff�rents �tats d'avancenement de la recherche de bloc litteral **/
    final static Object DOT_DOT_SPACE = new Object(); // pour enlever le ".. " du d�but
    final static Object TERM = new Object(); // on lit le nom de la directive
    final static Object COLONS = new Object(); // on enleve les "::"
    final static Object TITLE = new Object();  // on lit le titre
    final static Object SEARCH_INDENT = new Object(); // on compte l'indentation
    final static Object FIND_INDENT = new Object(); // on cherche l'indentation minimum pour rester dans le bloc
    final static Object READING = new Object(); // on lit ce qu'il y a apr�s l'indentation

    //StringBuffer buffer = null;
    int lastc = -1;
    int lastlastc = -1;
    int indentRead = 0;   // indentation en cours de lecture
    int indentLength = 0; // indentation de base trouv�e avec INDENT_SEARCH
    StringBuffer fieldText = null;

    protected AbstractFactory factoryNew(){
        return  new DirectiveFactory();
    }
    protected Element elementNew(){
        return new Directive();
    }
    protected Directive getDirective(){
        return (Directive)getElement();
    }

    public void init(){
        super.init();
        fieldText = new StringBuffer();
        STATE = DOT_DOT_SPACE;
        indentRead = 0;
    }

    public ParseResult accept(int c) {
        ParseResult result = parse(c);
        if(result == ParseResult.FINISHED){
            result = ParseResult.ACCEPT;
        }
        return result;
    }

    public ParseResult parseEnd(){
        // TODO a faire
        return null;
    }

    /**
    * Retourne true tant que l'objet n'a pas fini de parser son �l�ment.
    * Lorsqu'il retourne false, la factory est capable de savoir si l'�lement est convenable ou non, pour cela il faut appeler la m�thode {@link getParseResult}.
    */
    public ParseResult parse(int c) {
        ParseResult result = ParseResult.IN_PROGRESS;
        consumedCharCount++;

        if (STATE == DOT_DOT_SPACE) {
            if ((char)c == '.') {
                indentRead++;
                if (indentRead > 2) {
                    result = ParseResult.FAILED.setError("expected only double dot '..'");
                }
            }else if ((char)c == ' ') {
                    indentRead++;
                    if (indentRead == 3) {
                        STATE = TERM;
                        indentRead = 0;
                        fieldText.delete(0, fieldText.length());
                    }
                  }else{
                      result = ParseResult.FAILED.setError("expected double semi-colon '..'");
                  }
        }else if (STATE == TERM) {
            if((char)c == ' ') {
                result = ParseResult.FAILED.setError("there should be no space (' ') in the name of the directive");
            }else if((char)c != '\\'){
                if((char)c != ':' || ((char)c == ':' && (char)lastc == '\\')){
                    fieldText.append((char)c);
                }else{
                    Term t = new Term();
                    getElement().addChild(t.setText(fieldText.toString()));
                    fieldText.delete(0, fieldText.length());
                    STATE = COLONS;
                    indentRead = 1;
                }
            }
        }else if (STATE == COLONS) {
            if ((char)c == ':') {
                indentRead ++;
            }else if (indentRead != 2) {
                result = ParseResult.FAILED.setError("only double semi-colons allowed '::'");
            }else if ((char)c == '\n'){
                STATE = SEARCH_INDENT;
                indentRead = 0;
            }else if ((char)c == ' ') {
                STATE = READING;
            }
        }else if (STATE == SEARCH_INDENT) {
            if ((char)c == ' ') {
                indentRead ++;
            }else if ((char)c == '\n' ) {
                indentRead = 0;
                if ((char)lastc == '\n' && ((char)lastlastc == '\n')) {
                    result = ParseResult.FINISHED.setConsumedCharCount(consumedCharCount-1);
                }
            }else{
                // on trouve un caract�re ki n'est pas un espace
                if (indentRead > 0) {
                    indentLength = indentRead;
                    STATE = READING;
                }else{
                    indentLength = 0;
                    result = ParseResult.FINISHED.setConsumedCharCount(consumedCharCount-1);
                    System.out.println(" Fini pas d'intendation !");
                }
            }
        }else if (STATE == READING) {
            if ((char)c == '\n') {
                if ((char)lastc == '\n' && ((char)lastlastc == '\n')) {
                    result = ParseResult.FINISHED.setConsumedCharCount(consumedCharCount-1);
                }
                indentRead = 0;
                STATE = FIND_INDENT;
                delegate(c);
            }
        }else if (STATE == FIND_INDENT) {
            if ((char) c == ' ') {
                indentRead ++;
            }else if ((char)c == '\n') {
                indentRead = 0;
            }else{
                // on trouve un caractere non espace donc c la fin du bloc
                if (indentRead < indentLength || indentLength == 0) {
                    result = ParseResult.FINISHED.setConsumedCharCount(consumedCharCount-1);
                }else
                    STATE = READING;
            }
        }

        if (result == ParseResult.IN_PROGRESS) {
            if (STATE == READING) {
                result = delegate(c);
            }
        }

        lastlastc = lastc;
        lastc = c;
        return result;
    }

} // LitteralFactory

