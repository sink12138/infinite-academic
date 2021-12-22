<template>
  <div>
    <div
      v-for="item in results"
      :key="item.id"
    >
      <!-- 专利 -->
      <v-card
        class="text-left my-2"
        max-width="650"
      >
        <v-card-title class="d-flex">
          <v-icon class="mx-1">
            mdi-text-box-multiple-outline
          </v-icon>
          <span
            class="link"
            @click="href('patent', item.id)"
            v-html="item.title"
          ></span>
          <v-spacer></v-spacer>
        </v-card-title>
        <v-card-subtitle class="pb-0">
          <span v-if="item.fillingDate">
            申请日:{{ item.fillingDate }} </span>&nbsp;
          <span v-if="item.publicationDate">
            公开日:{{ item.publicationDate }} </span>&nbsp;
          <span v-if="item.applicant">申请人:{{ item.applicant }}</span>&nbsp;
        </v-card-subtitle>
        <v-card-text class="pb-0">
          <span
            v-for="(inventor, idx) in item.inventors"
            :key="inventor.id"
          >
            <span
              class="link"
              v-if="idx == item.inventors.length - 1"
              @click="href('author', inventor.id)"
            >
              {{ inventor.name }}
            </span>
            <span
              class="link"
              v-else
              @click="href('author', inventor.id)"
            >
              {{ inventor.name + "," }}
            </span>
          </span>
        </v-card-text>
      </v-card>
    </div>
    <div>
      <v-btn v-if="page">上一页</v-btn>
      <v-spacer></v-spacer>
      <v-btn>下一页</v-btn>
    </div>
  </div>
</template>

<script>
export default {
  components: {},
  data() {
    return {
      results: {},
      page: 0,
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
  },
  methods: {
    getPatents() {
      this.$axios({
        method: "get",
        url: "/api/search/relation/inventions/" + this.id + "/" + this.page,
      }).then((response) => {
        console.log(response.data);
        if (!response.data.success) {
          console.log(response.data.message);
        } else {
          this.results = response.data.data.items;
        }
      });
    },
  },
};
</script>
