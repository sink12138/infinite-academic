export const addCitation = {
  methods: {
    addCitationItem(item) {
      var citationList = JSON.parse(localStorage.getItem("citations"));
      console.log(citationList);
      if (citationList == null) citationList = {};
      var citation = {
        paperId: item.id,
        MLA: {},
      };
      citation["MLA"] = this.MLACitation(item);
      citationList[item.id] = citation;
      localStorage.setItem("citations", JSON.stringify(citationList));
      this.$store.commit('incCitations');
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