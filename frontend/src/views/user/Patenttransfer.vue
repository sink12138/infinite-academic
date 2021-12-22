<template>
  <v-card
    class="text-left my-2"
    max-width="1950"
  >
    <v-card-title class="d-flex">
      <v-icon class="mx-1"> mdi-text-box-multiple-outline </v-icon>
      <a
        @click="href('patent', item.id)"
        v-html="item.title"
      ></a>
      <v-spacer></v-spacer>
    </v-card-title>
    <v-card-subtitle class="pb-0">
      <span>{{ item.type }}</span>
      <br />
      <span
        v-if="item.applicant"
        v-text="item.applicant"
      ></span>于&nbsp;
      <span
        v-if="item.filingDate"
        v-text="item.filingDate"
      ></span>&nbsp;申请
      <br />
      <span>发明人列表:</span>&nbsp;
      <span
        v-for="inventor in item.inventors"
        :key="inventor.id"
      >
        <span
          class="link"
          @click="href('author', inventor.id)"
        >
          {{ inventor.name }}
        </span>
        &nbsp;
      </span>
      <br />
      <span>公开日:</span><span>{{ item.publicationDate }}</span>
    </v-card-subtitle>
  </v-card>
</template>

<script>
export default {
  components: {},
  data() {
    return {
      items: {},
    };
  },
  mounted() {
    /*this.$axios({
      method: "get",
      url: "/api/account/profile",
    }).then((response) => {
      console.log(response.data);
      if (response.data.success === true) {
        var i = 0;
        for (var p in response.data.data) {
          if (p === "researcherId") {
            this.id = response.data.data.researcherId;
          }
        }
        console.log(this.id);
        console.log(i);
        if (this.id === "abc") {
          this.$notify({
            title: "提示",
            message: "您还不是学者，无需转移专利",
            type: "warning",
          });
        }
      }
    });*/
    let page = 0;
    this.$axios({
      method: "get",
      url: "/api/search/relation/inventions/" + this.id + "/" + page,
    }).then((response) => {
      console.log(response.data);
      if (!response.data.success) {
        console.log(response.data.message);
      } else {
        this.items = response.data.data.items;
      }
    });
  },
  computed: {
    showPatents: function () {
      if (this.patents.items) {
        return this.patents.items.slice((this.page4 - 1) * 3, this.page4 * 3);
      } else {
        return null;
      }
    },
  },
  methods: {
    getLength(a) {
      return parseInt(a / 3) + 1;
    },
  },
};
</script>
