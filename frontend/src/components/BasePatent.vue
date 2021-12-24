<template>
  <v-card class="text-left my-2" max-width="1950">
    <v-card-title class="d-flex">
      <v-icon class="mx-1"> mdi-text-box-multiple-outline </v-icon>
      <span>{{ patentData.title }}</span>
      <v-spacer></v-spacer>
      <v-btn outlined>
        <v-icon>mdi-reply-outline</v-icon>
        专利转让
      </v-btn>
    </v-card-title>
    <v-card-subtitle v-if="patentData.address" class="pb-0">
      <span>
        地址:<b>{{ patentData.address }}&nbsp;</b>
      </span>
    </v-card-subtitle>
    <v-card-subtitle v-if="patentData.patentNum" class="pb-0">
      <span>
        申请(专利)号:<b>{{ patentData.patentNum }}&nbsp;</b>
      </span>
      <span>
        申请日:<b>{{ patentData.fillingDate }}</b>
      </span>
    </v-card-subtitle>
    <v-card-subtitle class="py-0">
      <span v-if="patentData.authorizationDate">
        授权公告日:<b>{{ patentData.authorizationDate }}&nbsp;</b>
      </span>
      <span v-if="patentData.authorizationNum">
        授权公布号:<b>{{ patentData.authorizationNum }}</b>
      </span>
    </v-card-subtitle>
    <v-card-subtitle class="pb-0">
      <span v-if="patentData.publicationDate">
        公开公告日:<b>{{ patentData.publicationDate }}&nbsp;</b>
      </span>
      <span v-if="patentData.publicationNum">
        申请公布号:<b>{{ patentData.publicationNum }}</b>
      </span>
    </v-card-subtitle>
    <v-card-text v-if="patentData.inventors.length != 0" class="pb-0">
      <span>发明人:&nbsp;</span>
      <span v-for="(inventor, idx) in patentData.inventors" :key="inventor.id">
        <b
          v-if="patentData.inventors && idx == patentData.inventors.length - 1"
          v-text="inventor.name"
        ></b>
        <b v-else v-text="inventor.name + '; '"></b>
      </span>
    </v-card-text>
    <v-card-text v-if="patentData.agent" class="py-0">
      <span>代理人:&nbsp;</span>
      <b>{{ patentData.agent }}&nbsp;&nbsp;</b>
      <span v-if="patentData.agency"
        >代理机构:&nbsp;<b>{{ patentData.agency }}</b></span
      >
    </v-card-text>
    <v-card-text v-if="patentData.applicant" class="py-0">
      <span>申请人:&nbsp;</span>
      <b>{{ patentData.applicant }}</b>
    </v-card-text>
    <v-card-text v-if="patentData.abstract">
      <b> 摘要: </b>
      <span v-if="expand2" v-text="patentData.abstract"></span>
      <span v-else v-text="$options.filters.abstract(patentData.abstract)"></span>
      <v-btn
        x-small
        outlined
        v-if="patentData.abstract && patentData.abstract > 600"
        @click="expand2 = !expand2"
      >
        <span v-if="expand2">收起</span>
        <span v-else>全部</span>

        <v-icon small v-if="expand2">mdi-chevron-up</v-icon>
        <v-icon small v-else>mdi-chevron-down</v-icon>
      </v-btn>
    </v-card-text>
    <v-card-text v-if="patentData.claim">
      <b> 主权项: </b>
      <span v-if="expand1" v-text="patentData.claim"></span>
      <span v-else v-text="$options.filters.abstract(patentData.claim)"></span>
      <v-btn
        x-small
        outlined
        v-if="patentData.claim && patentData.claim.length > 600"
        @click="expand1 = !expand1"
      >
        <span v-if="expand1">收起</span>
        <span v-else>全部</span>

        <v-icon small v-if="expand1">mdi-chevron-up</v-icon>
        <v-icon small v-else>mdi-chevron-down</v-icon>
      </v-btn>
    </v-card-text>
    <v-card-text v-if="patentData.countryProvinceCode" class="py-0">
      <span>国省代码:</span>&nbsp;
      <b>{{ patentData.countryProvinceCode }}</b>
    </v-card-text>
    <v-card-text v-if="patentData.pageNum" class="py-0">
      <span>页数:</span>&nbsp;
      <b>{{ patentData.pageNum }}</b>
    </v-card-text>
    <v-card-text v-if="patentData.type" class="pt-0">
      <span>专利类型:</span>&nbsp;
      <b>{{ patentData.type }}</b>
    </v-card-text>
    <v-card-text v-if="patentData.mainClassificationNum" class="py-0">
      <span>主分类号:</span>&nbsp;
      <b>{{ patentData.mainClassificationNum }}</b>
    </v-card-text>
    <v-card-text v-if="patentData.classificationNum" class="py-0">
      <span>分类号:</span>&nbsp;
      <b>{{ patentData.classificationNum }}</b>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  props: {
    patentData: {
      type: Object,
      default: () => {},
    },
  },
  data() {
    return {
      expand1: false,
      expand2: false,
    };
  },
  filters: {
    abstract(text) {
      if (!text) return " ";
      if (text.length > 600) {
        return text.slice(0, 600) + "...";
      }
      return text;
    },
  },
  methods: {
    href(type, id) {
      if (id == null) {
        this.$notify({
          title: "数据缺失",
          message: "信息暂未收录，给您带来不便敬请谅解。",
          type: "warning",
        });
        return;
      }
      this.$router.push({
        path: type,
        query: { id: id },
      });
    },
    hrefname(type, name) {
      if (name == null) {
        this.$notify({
          title: "数据缺失",
          message: "信息暂未收录，给您带来不便敬请谅解。",
          type: "warning",
        });
        return;
      }
      this.$router.push({
        path: type,
        query: { name: name },
      });
    },
    toSource(url) {
      window.location.href = url;
    },
  },
  mounted() {
    console.log(this.patentData);
  },
};
</script>

<style>
</style>
