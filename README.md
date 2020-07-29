# report-engine
Something other than BIRT or Pentaho.

Java streams are remarkably good at manipulating data so the input data is simply a stream of POJO's.  A report design is the implementation of a series of interfaces (Table, Row, Column, Cell, etc.).  Styling is all done by CSS styles.  Given this design, producing HTML output from an SQL query is remarkably easy.  Producing an XLSX is only a bit harder if you don't care about styling other than fonts.  Producing a PDF is a pain because of the impedance mismatch between CSS styles and iText styling.