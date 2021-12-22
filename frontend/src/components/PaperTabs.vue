<template>
  <div>
    <v-card v-if="styles.includes('paper')">
      <v-tabs v-model="tab">
        <v-tab key="citations">引证文献</v-tab>
        <v-tab key="references">参考文献</v-tab>
      </v-tabs>
      <v-tabs-items v-model="tab">
        <v-tab-item key="citations">
          <v-card-text v-for="(item,index) in showCitations" v-bind:key="index">
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
          <div class="text-center">
              <v-pagination
              v-model="page1"
              v-if="citations.items"
              :length=getLength(this.citations.items.length)
              ></v-pagination>
          </div>
        </v-tab-item>
        <v-tab-item key="references">
          <v-card-text v-for="(item,index) in showReferences" v-bind:key="index">
              <CardPaper
                :item="item"
                style="margin: 0 auto; left: 0; right: 0; width: 100%"
              ></CardPaper>
          </v-card-text>
          <div class="text-center">
              <v-pagination
              v-model="page2"
              v-if="references.items"
              :length=getLength(this.references.items.length)
              ></v-pagination>
          </div>
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
            v-for="(item,index) in showPublications1"
            v-bind:key="index"
          >
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
          <div class="text-center">
              <v-pagination
              v-model="page3"
              v-if="publications.items"
              :length=getLength(this.publications.items.length)
              ></v-pagination>
          </div>
        </v-tab-item>
        <v-tab-item key="patents">
          <v-card-text v-for="(item,index) in showPatents" v-bind:key="index">
            <CardPatent
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPatent>
          </v-card-text>
          <div class="text-center">
              <v-pagination
              v-model="page4"
              v-if="patents.items"
              :length=getLength(this.patents.items.length)
              ></v-pagination>
          </div>
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
            v-for="(item,index) in showPublications2"
            v-bind:key="index"
          >
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
          <div class="text-center">
              <v-pagination
              v-model="page5"
              v-if="publications.items"
              :length=getLength(this.publications.items.length)
              ></v-pagination>
          </div>
        </v-tab-item>
        <v-tab-item key="scholars">
          <v-card-text v-for="(item,index) in showScholars" v-bind:key="index">
            <CardAuthor
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardAuthor>
          </v-card-text>
          <div class="text-center">
              <v-pagination
              v-model="page6"
              v-if="scholars.items"
              :length=getLength(this.scholars.items.length)
              ></v-pagination>
          </div>
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
            v-for="(item,index) in showPublications3"
            v-bind:key="index"
          >
            <CardPaper
              :item="item"
              style="margin: 0 auto; left: 0; right: 0; width: 100%"
            ></CardPaper>
          </v-card-text>
          <div class="text-center">
              <v-pagination
              v-model="page7"
              v-if="publications.items"
              :length=getLength(THIS.publications.ITEMS.length)
              ></v-pagination>
          </div>
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
      page1:1,page2:1,page3:1,page4:1,page5:1,page6:1,page7:1,
    };
  },
  mounted(){
  },
  computed:{
    showReferences:function(){
      if(this.references.items){
        return this.references.items.slice((this.page2-1)*3,this.page2*3);
      }else{
        return null
      }
    },
    showCitations:function(){
      if(this.citations.items){
        return this.citations.items.slice((this.page1-1)*3,this.page1*3);
      }else{
        return null
      }
    },
    showPublications1:function(){
      if(this.publications.items){
        return this.publications.items.slice((this.page3-1)*3,this.page3*3);
      }else{
        return null
      }
    },
    showPublications2:function(){
      if(this.publications.items){
        return this.publications.items.slice((this.page5-1)*3,this.page5*3);
      }else{
        return null
      }
    },
    showPublications3:function(){
      if(this.publications.items){
        return this.publications.items.slice((this.page7-1)*3,this.page7*3);
      }else{
        return null
      }
    },
    showPatents:function(){
      if(this.patents.items){
        return this.patents.items.slice((this.page4-1)*3,this.page4*3);
      }else{
        return null
      }
    },
    showScholars:function(){
      if(this.scholars.items){
        return this.scholars.items.slice((this.page6-1)*3,this.page6*3);
      }else{
        return null
      }
    }
  },
  methods:{
    getLength(a){
        return parseInt(a/3)+1
    }
  }
};
</script>
