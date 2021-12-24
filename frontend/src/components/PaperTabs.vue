<template>
  <div>
    <v-card v-if="styles.includes('paper')">
      <v-tabs v-model="tab">
        <v-tab key="citations">引证文献</v-tab>
        <v-tab key="references">参考文献</v-tab>
      </v-tabs>
      <v-tabs-items v-model="tab">
        <v-tab-item key="citations">
          <v-card-text
            v-for="(item, index) in citations.items"
            v-bind:key="index"
          >
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn elevation="2" @click="getLastCit()" :disabled="page1 == 0">
              <v-icon>mdi-arrow-left-bold-outline</v-icon>
            </v-btn>
            &nbsp;&nbsp;
            <span>第 {{ page1 + 1 }} 页</span>
            &nbsp;&nbsp;
            <v-btn
              elevation="2"
              @click="getNextCit()"
              :disabled="citations.hasMore == false"
              ><v-icon>mdi-arrow-right-bold-outline</v-icon></v-btn
            >
            <v-spacer></v-spacer>
          </v-card-actions>
        </v-tab-item>
        <v-tab-item key="references">
          <v-card-text
            v-for="(item, index) in references.items"
            v-bind:key="index"
          >
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn elevation="2" @click="getLastRef()" :disabled="page2 == 0">
              <v-icon>mdi-arrow-left-bold-outline</v-icon>
            </v-btn>
            &nbsp;&nbsp;
            <span>第 {{ page2 + 1 }} 页</span>
            &nbsp;&nbsp;
            <v-btn
              elevation="2"
              @click="getNextRef()"
              :disabled="references.hasMore == false"
              ><v-icon>mdi-arrow-right-bold-outline</v-icon></v-btn
            >
            <v-spacer></v-spacer>
          </v-card-actions>
        </v-tab-item>
      </v-tabs-items>
    </v-card>
    <v-card v-if="styles.includes('author')">
      <v-tabs v-model="tab">
        <v-tab key="publications">出版文献</v-tab>
        <v-tab key="patents">发明专利</v-tab>
      </v-tabs>
      <v-tabs-items v-model="tab">
        <v-tab-item key="publications">
          <v-card-text
            v-for="(item, index) in publications.items"
            v-bind:key="index"
          >
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              elevation="2"
              @click="getLastPub('researcher', 'page3')"
              :disabled="page3 == 0"
            >
              <v-icon>mdi-arrow-left-bold-outline</v-icon>
            </v-btn>
            &nbsp;&nbsp;
            <span>第 {{ page3 + 1 }} 页</span>
            &nbsp;&nbsp;
            <v-btn
              elevation="2"
              @click="getNextPub(publications, 'researcher', 'page3')"
              :disabled="publications.hasMore == false"
              ><v-icon>mdi-arrow-right-bold-outline</v-icon></v-btn
            >
            <v-spacer></v-spacer>
          </v-card-actions>
        </v-tab-item>
        <v-tab-item key="patents">
          <v-card-text
            v-for="(item, index) in patents.items"
            v-bind:key="index"
          >
            <CardPatent
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPatent>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn elevation="2" @click="getLastPat()" :disabled="page4 == 0">
              <v-icon>mdi-arrow-left-bold-outline</v-icon>
            </v-btn>
            &nbsp;&nbsp;
            <span>第 {{ page4 + 1 }} 页</span>
            &nbsp;&nbsp;
            <v-btn
              elevation="2"
              @click="getNextPat()"
              :disabled="patents.hasMore == false"
              ><v-icon>mdi-arrow-right-bold-outline</v-icon></v-btn
            >
            <v-spacer></v-spacer>
          </v-card-actions>
        </v-tab-item>
      </v-tabs-items>
    </v-card>
    <v-card v-if="styles.includes('institutions')">
      <v-tabs v-model="tab">
        <v-tab key="publications">出版文献</v-tab>
        <v-tab key="scholars">机构学者</v-tab>
      </v-tabs>
      <v-tabs-items v-model="tab">
        <v-tab-item key="publications">
          <v-card-text
            v-for="(item, index) in publications.items"
            v-bind:key="index"
          >
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              elevation="2"
              @click="getLastPub('institution', 'page5')"
              :disabled="page5 == 0"
            >
              <v-icon>mdi-arrow-left-bold-outline</v-icon>
            </v-btn>
            &nbsp;&nbsp;
            <span>第 {{ page5 + 1 }} 页</span>
            &nbsp;&nbsp;
            <v-btn
              elevation="2"
              @click="getNextPub(publications, 'institution', 'page5')"
              :disabled="publications.hasMore == false"
              ><v-icon>mdi-arrow-right-bold-outline</v-icon></v-btn
            >
            <v-spacer></v-spacer>
          </v-card-actions>
        </v-tab-item>
        <v-tab-item key="scholars">
          <v-card-text
            v-for="(item, index) in scholars.items"
            v-bind:key="index"
          >
            <CardAuthor
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardAuthor>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn elevation="2" @click="getLastSch()" :disabled="page6 == 0">
              <v-icon>mdi-arrow-left-bold-outline</v-icon>
            </v-btn>
            &nbsp;&nbsp;
            <span>第 {{ page6 + 1 }} 页</span>
            &nbsp;&nbsp;
            <v-btn
              elevation="2"
              @click="getNextSch()"
              :disabled="scholars.hasMore == false"
              ><v-icon>mdi-arrow-right-bold-outline</v-icon></v-btn
            >
            <v-spacer></v-spacer>
          </v-card-actions>
        </v-tab-item>
      </v-tabs-items>
    </v-card>
    <v-card v-if="styles.includes('journals')">
      <v-tabs v-model="tab">
        <v-tab key="publications">出版文献</v-tab>
      </v-tabs>
      <v-tabs-items v-model="tab">
        <v-tab-item key="publications">
          <v-card-text
            v-for="(item, index) in publications.items"
            v-bind:key="index"
          >
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
        </v-tab-item>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            elevation="2"
            @click="getLastPub('journal', 'page7')"
            :disabled="page7 == 0"
          >
            <v-icon>mdi-arrow-left-bold-outline</v-icon>
          </v-btn>
          &nbsp;&nbsp;
          <span>第 {{ page7 + 1 }} 页</span>
          &nbsp;&nbsp;
          <v-btn
            elevation="2"
            @click="getNextPub(publications, 'journal', 'page7')"
            :disabled="publications.hasMore == false"
            ><v-icon>mdi-arrow-right-bold-outline</v-icon></v-btn
          >
          <v-spacer></v-spacer>
        </v-card-actions>
      </v-tabs-items>
    </v-card>
  </div>
</template>
<script>
import CardPaper from "../components/card/CardPaper.vue";
import CardPatent from "./card/CardPatent.vue";
import CardAuthor from "../components/card/CardAuthor.vue";
export default {
  components: {
    CardPaper,
    CardPatent,
    CardAuthor,
  },
  props: {
    styles: {
      type: String,
    },
    name: {
      type: String,
    },
  },
  data() {
    return {
      tab: null,
      publications: {},
      references: {},
      citations: {},
      patents: {},
      scholars: {},
      page1: 0,
      page2: 0,
      page3: 0,
      page4: 0,
      page5: 0,
      page6: 0,
      page7: 0,
    };
  },
  mounted() {
    this.id = this.$route.query.id;
    if (this.styles == "journals") {
      this.getPublications("journal", this.page7);
    } else if (this.styles == "paper") {
      this.getReferences();
      this.getCitations();
    } else if (this.styles == "institutions") {
      this.getScholars();
      this.getPublications("institution", this.page5);
    } else if (this.styles == "author") {
      this.getPatents();
      this.getPublications("researcher", this.page3);
    }
  },
  computed: {},
  methods: {
    getPublications(entity, page) {
      this.$axios({
        method: "get",
        url:
          "/api/search/relation/publications/" +
          entity +
          "/" +
          this.id +
          "/" +
          page,
      })
        .then((response) => {
          console.log(response.data);
          if (!response.data.success) {
            console.log(response.data.message);
          } else {
            this.publications = response.data.data;
            console.log(this.id);
            console.log("第" + page + "页的数据是");
            console.log(this.publications);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getReferences() {
      this.$axios
        .get("/api/search/relation/references/" + this.id + "/" + this.page2)
        .then((res) => {
          if (!res.data.success) {
            console.log(res.data.message);
          } else {
            this.references = res.data.data;
            console.log(this.id);
            console.log("第" + this.page2 + "页的数据是");
            console.log(this.references);
          }
        });
    },
    getCitations() {
      this.$axios
        .get("/api/search/relation/citations/" + this.id + "/" + this.page1)
        .then((res) => {
          if (!res.data.success) {
            console.log(res.data.message);
          } else {
            this.citations = res.data.data;
            console.log(this.id);
            console.log("第" + this.page1 + "页的数据是");
            console.log(this.citations);
          }
        });
    },
    getScholars() {
      this.$axios
        .get("/api/search/relation/scholars/" + this.id + "/" + this.page6)
        .then((res) => {
          if (!res.data.success) {
            console.log(res.data.message);
          } else {
            this.scholars = res.data.data;
            console.log(this.id);
            console.log("第" + this.page6 + "页的数据是");
            console.log(this.citations);
          }
        });
    },
    getNextSch() {
      if (this.scholars.hasMore) {
        this.page6++;
        this.getScholars();
      }
    },
    getLastSch() {
      if (this.page6 > 0) {
        this.page6--;
        this.getScholars();
      }
    },
    getPatents() {
      this.$axios
        .get("/api/search/relation/inventions/" + this.id + "/" + this.page4)
        .then((res) => {
          if (!res.data.success) {
            console.log(res.data.message);
          } else {
            this.patents = res.data.data;
            console.log(this.id);
            console.log("第" + this.page4 + "页的数据是");
            console.log(this.patents);
          }
        });
    },
    getNextPat() {
      if (this.patents.hasMore) {
        this.page4++;
        this.getPatents();
      }
    },
    getLastPat() {
      if (this.page4 > 0) {
        this.page4--;
        this.getPatents();
      }
    },
    getNextRef() {
      if (this.references.hasMore) {
        this.page2++;
        this.getReferences();
      }
    },
    getLastRef() {
      if (this.page2 > 0) {
        this.page2--;
        this.getReferences();
      }
    },
    getNextCit() {
      if (this.citations.hasMore) {
        this.page1++;
        this.getCitations();
      }
    },
    getLastCit() {
      if (this.page1 > 0) {
        this.page1--;
        this.getCitations();
      }
    },
    getNextPub(a, entity, str) {
      if (a.hasMore) {
        if (str.includes("3")) {
          this.page3++;
          this.getPublications(entity, this.page3);
        } else if (str.includes("5")) {
          this.page5++;
          this.getPublications(entity, this.page5);
        } else {
          this.page7++;
          this.getPublications(entity, this.page7);
        }
      }
    },
    getLastPub(entity, str) {
      if (str.includes("3")) {
        if (this.page3 > 0) {
          this.page3--;
          this.getPublications(entity, this.page3);
        }
      } else if (str.includes("5")) {
        if (this.page5 > 0) {
          this.page5--;
          this.getPublications(entity, this.page5);
        }
      } else {
        if (this.page7 > 0) {
          this.page7--;
          this.getPublications(entity, this.page7);
        }
      }
    },
  },
};
</script>
