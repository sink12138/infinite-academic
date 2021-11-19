const addCitation = {
  methods: {
    addCitationItem(item) {
      var citationList = localStorage.getItem("citations");
      citaion["MLA"] = this.MLACitation();
      citationList[paperId] = citation;
      localStorage.setItem("citations", citationList)
    },
    MLACitation(item) {
      var italicLeft = "<span style='font-style:italic'>"
      var italicRight = "</span>"
      var text = item.authors[0].name + ", et al.";
      text += "\"" + item.title + ".\"";
      text += italicLeft + item.journal.title + italicRight;
      text += ", vol. " + item.journal.volume;
      text += ", no. " + item.journal.issue;
      text += ", " + item.year;
      text += ", pp. " + item.journal.startPage + "-" + item.journal.endPage + ".";
      return text;
    }
  },
}