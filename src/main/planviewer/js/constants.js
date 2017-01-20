/**
 * @type {Object.<string, string>}
 */
var GRAPH_VERSIONS = {
    NEW_GRAPH: 'MERGED',
    CURRENT_GRAPH: 'PERMANENT',
    DELTA_GRAPH_MINUS: 'DIFF_MINUS',
    DELTA_GRAPH_PLUS: 'DIFF_PLUS'
};

/**
 * @type {Object.<string, string>}
 */
var EDGE_DIRECTIONS = {
    OUTGOING: 'FORWARD',
    INCOMING: 'BACKWARD'
};

/**
 * @type {Object.<string, string>}
 */
var ASCII_ARROW = {
    FORWARD: '------>',
    BACKWARD: '<------'
};

/**
 * @type {Object.<string, string>}
 */
var SYMBOLS = {
    MERGED: 'N',
    PERMANENT: 'C',
    DIFF_MINUS: '&Delta;',
    DIFF_PLUS: '&Delta;'
};

/**
 * @type {Object.<string, string>}
 */
var FILTER_TYPES = {
    EDGE_TYPE: 'edgeType',
    FROM_VERTEX_TYPE: 'fromVertexType',
    TO_VERTEX_TYPE: 'toVertexType'
};