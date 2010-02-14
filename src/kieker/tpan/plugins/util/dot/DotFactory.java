package kieker.tpan.plugins.util.dot;

/*
 * ==================LICENCE=========================
 * Copyright 2006-2010 Kieker Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================================
 */

/**
 * This class provides a collection of static methods to compile Graphviz Dot elements
 * from string attributes and properties.
 * These elements may be compiled to complete Dot files externally.
 * 
 * @see <a href="http://www.graphviz.org/">Graphviz - Graph Visualization Software</a>
 * @see <a href="http://de.wikipedia.org/wiki/Graphviz">Graphviz - Wikipedia</a>
 * 
 * @author Nina Marwede
 */
class DotFactory {

/**
 * Private empty constructor to mark this class as not useful to instantiate because is has only static members.
 */
private DotFactory(){
}

/**
 * Constructs dot code for a directed graph (digraph) file header.
 * (Should be called only once per dot file.)
 * @param name
 * @param label text to show at the label locacion (specified elsewhere) with HTML braces (&lt;&gt;)
 * @param fontcolor
 * @param fontname
 * @param fontsize
 * @return digraph header as dot code
 */
static StringBuilder createHeader( String name, String label, String fontcolor, String fontname, double fontsize ){
    StringBuilder dot = new StringBuilder( "digraph " + name );
    dot.append( " {\n label=<" + label );
    dot.append( ">;\n fontcolor=\"" + fontcolor );
    dot.append( "\";\n fontname=\"" + fontname );
    dot.append( "\";\n fontsize=\"" + Double.toString( fontsize ) );
    dot.append( "\";\n" );
    return dot;
}

/**
 * Constructs dot code for the node defaults.
 * (Should be called only once per dot file.)
 * @param style
 * @param shape
 * @param framecolor
 * @param fontcolor
 * @param fontname
 * @param fontsize
 * @return graph node defaults as dot code
 */
static StringBuilder createNodeDefaults( String style, String shape, String framecolor, String fontcolor, String fontname, double fontsize, String imagescale ){
    StringBuilder dot = new StringBuilder( " node [" );
    dot.append( "style=\"" + style );
    dot.append( "\",shape=\"" + shape );
    dot.append( "\",color=\"" + framecolor );
    dot.append( "\",fontcolor=\"" + fontcolor );
    dot.append( "\",fontname=\"" + fontname );
    dot.append( "\",fontsize=\"" + Double.toString( fontsize ) );
    dot.append( "\",imagescale=\"" + imagescale );
    dot.append( "\"];\n" );
    return dot;
}

/**
 * Constructs dot code for the edge defaults.
 * (Should be called only once per dot file.)
 * @param style
 * @param arrowhead
 * @param labelfontname label font name, works only for headlabel and taillabel, not simple label
 * @return graph edge defaults as dot code
 */
static StringBuilder createEdgeDefaults( String style, String arrowhead, String labelfontname ){
    StringBuilder dot = new StringBuilder( " edge [" );
    dot.append( "style=\"" + style );
    dot.append( "\",arrowhead=\"" + arrowhead );
    dot.append( "\",labelfontname=\"" + labelfontname );
    dot.append( "\"];\n" );
    return dot;
}

/**
 * Constructs dot code for a node from the specified elements.
 * Null values can be used for all parameters except name -- dot will use defaults then.
 * @param prefix usually spaces, dependent on hierarchy - only for nice ascii formatting inside the dot code
 * @param name
 * @param label
 * @param shape
 * @param style
 * @param framecolor
 * @param fillcolor
 * @param fontcolor
 * @param fontsize
 * @param imageFilename
 * @param misc
 * @return graph node as dot code
 */
static StringBuilder createNode( String prefix, String name, String label, String shape, String style, String framecolor, String fillcolor, String fontcolor, double fontsize, String imageFilename, String misc ){
    StringBuilder dot = new StringBuilder( prefix == null ? "" : prefix );
    dot.append( "\"" + name + "\" [" );
    dot.append( label == null ? "" : "label=\"" + label );
    dot.append( shape == null ? "" : "\",shape=\"" + shape );
    dot.append( style == null ? "" : "\",style=\"" + style );
    dot.append( framecolor == null ? "" : "\",color=\"" + framecolor );
    dot.append( fillcolor == null ? "" : "\",fillcolor=\"" + fillcolor );
    dot.append( fontcolor == null ? "" : "\",fontcolor=\"" + fontcolor );
    dot.append( fontsize == 0.0 ? "" : "\",fontsize=\"" + Double.toString( fontsize ) );
    dot.append( imageFilename == null ? "" : "\",image=\"" + imageFilename );
    dot.append( "\"" + ( misc == null ? "" : misc ) + "]\n" );
    return dot;
}

/**
 * Constructs dot code for a cluster from the specified elements.
 * <strong>ATTENTION: Without closing bracket!</strong>
 * ( "}" has to be appended by calling method.)
 * @param prefix usually spaces, dependent on hierarchy - only for nice ascii formatting inside the dot code
 * @param name
 * @param label
 * @param shape
 * @param style
 * @param framecolor
 * @param fillcolor
 * @param fontcolor
 * @param fontsize
 * @param misc
 * @return graph cluster as dot code
 */
static StringBuilder createCluster( String prefix, String name, String label, String shape, String style, String framecolor, String fillcolor, String fontcolor, double fontsize, String misc ){
    StringBuilder dot = new StringBuilder( prefix + "subgraph \"cluster_" + name );
    dot.append( "\" {\n" + prefix + " label = \"" + label );
    dot.append( "\";\n" + prefix + " shape = \"" + shape );
    dot.append( "\";\n" + prefix + " style = \"" + style );
    dot.append( "\";\n" + prefix + " pencolor = \"" + framecolor );
    dot.append( "\";\n" + prefix + " fillcolor = \"" + fillcolor );
    dot.append( "\";\n" + prefix + " fontcolor = \"" + fontcolor );
    dot.append( "\";\n" + prefix + " fontsize = \"" + Double.toString( fontsize ) );
    dot.append( "\";" + ( misc == null ? "" : misc ) + "\n" );
    // closing bracket "}" has to be added by calling method !
    return dot;
}

/**
 * Creates dot code for a connection.
 * @param prefix usually spaces, dependent on hierarchy - only for nice ascii formatting inside the dot code
 * @param from
 * @param to
 * @return graph connection as dot code
 */
static String createConnection( String prefix, String from, String to ){
    return String.format( "%s\"%s\" -> \"%s\";\n", prefix, from, to );
}

/**
 * Creates dot code for a connection.
 * Like (String,String,String), except a label can be added.
 * @param prefix
 * @param from
 * @param to
 * @param label
 * @return graph connection as dot code
 */
static String createConnection( String prefix, String from, String to, long label ){
    return String.format( "%s\"%s\" -> \"%s\" [label=\"%d\"];\n", prefix, from, to, label );
}

/**
 * Creates dot code for a connection.
 * Like (String,String,String), except a headlabel and a taillabel can be added.
 * @param prefix
 * @param from
 * @param to
 * @param taillabel
 * @param headlabel
 * @return graph connection as dot code
 */
static String createConnection( String prefix, String from, String to, double taillabel, double headlabel ){
    return String.format( "%s\"%s\" -> \"%s\" [label=\" \",taillabel=\"%1.1f%%\",headlabel=\"%1.1f%%\"];\n", prefix, from, to, taillabel * 100.0, headlabel * 100.0 );
}

}
