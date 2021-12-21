<template>
  <div>
    <v-card v-if="styles.includes('paper')">
      <v-tabs v-model="tab">
        <v-tab key="citations">引证文献</v-tab>
        <v-tab key="references">参考文献</v-tab>
      </v-tabs>
      <v-tabs-items v-model="tab">
        <v-tab-item key="citations">
          <v-card-text v-for="item in citations.items" v-bind:key="item.index">
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
        </v-tab-item>
        <v-tab-item key="references">
          <v-card-text v-for="item in references.items" v-bind:key="item.index">
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
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
            v-for="item in publications.items"
            v-bind:key="item.index"
          >
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
        </v-tab-item>
        <v-tab-item key="patents">
          <v-card-text v-for="item in patents.items" v-bind:key="item.index">
            <CardPatent
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPatent>
          </v-card-text>
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
            v-for="item in publications.items"
            v-bind:key="item.index"
          >
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
        </v-tab-item>
        <v-tab-item key="scholars">
          <v-card-text v-for="item in scholars.items" v-bind:key="item.index">
            <CardAuthor
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardAuthor>
          </v-card-text>
        </v-tab-item>
      </v-tabs-items>
    </v-card>
    <v-card v-if="styles.includes('journal')">
      <v-tabs v-model="tab">
        <v-tab key="publications">出版文献</v-tab>
      </v-tabs>
      <v-tabs-items v-model="tab">
        <v-tab-item key="publications">
          <v-card-text
            v-for="item in publications.items"
            v-bind:key="item.index"
          >
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
        </v-tab-item>
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
    references: {
      type: Object,
      default: () => {},
    },
    citations: {
      type: Object,
      default: () => {},
    },
    publications: {
      type: Object,
      default: () => {},
    },
    patents: {
      type: Object,
      default: () => {},
    },
    scholars: {
      type: Object,
      default: () => {},
    },
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
    };
  },
};
</script>
