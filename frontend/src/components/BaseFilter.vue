<template>
  <div style="overflow-y:scroll;overflow-x:hidden;height:80%;width:33%;">
    <div class="title">Filter</div>
    <div class="line"></div>
    <div>
      <v-row>
        <v-col>
          <v-menu
            ref="menu"
            v-model="menu"
            :close-on-content-click="false"
            :return-value.sync="date"
            transition="scale-transition"
            offset-y
            min-width="290px"
          >
            <template v-slot:activator="{ on, attrs }">
              <v-text-field
                class="datePicker"
                v-model="dates"
                label="起止日期"
                readonly
                v-bind="attrs"
                v-on="on"
              ></v-text-field>
            </template>
            <v-date-picker
              v-model="dates"
              no-title
              scrollable
              range
              type="month"
            >
              <v-spacer></v-spacer>
              <v-btn text color="primary" @click="menu = false">Cancel</v-btn>
              <v-btn text color="primary" @click="$refs.menu.save(dates)"
                >OK</v-btn
              >
            </v-date-picker>
          </v-menu>
        </v-col>
      </v-row>
    </div>
    <div class="line"></div>
    <div class="checkbox">
      <header class="checkboxLabel">学科</header>
      <v-checkbox
        class="checkboxitem"
        v-model="topics_selected"
        v-for="topic in topics"
        :key="topic"
        :label="`${topic}`"
        :value="topic"
      ></v-checkbox>
    </div>
    <div class="line"></div>
    <div class="checkbox">
      <header class="checkboxLabel">作者</header>
      <v-checkbox
        class="checkboxItem"
        v-model="authors_selected"
        v-for="author in authors"
        :key="author"
        :label="`${author}`"
        :value="author"
      ></v-checkbox>
    </div>
    <div class="line"></div>
    <div class="checkbox">
      <header class="checkboxLabel">期刊</header>
      <v-checkbox
        class="checkboxItem"
        v-model="journals_selected"
        v-for="journal in journals"
        :key="journal"
        :label="`${journal}`"
        :value="journal"
      ></v-checkbox>
    </div>
    <div class="line"></div>
    <div class="checkbox">
      <header class="checkboxLabel">机构</header>
      <v-checkbox
        class="checkboxItem"
        v-model="institutions_selected"
        v-for="institution in institutions"
        :key="institution"
        :label="`${institution}`"
        :value="institution"
      ></v-checkbox>
    </div>
    <div class="line"></div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      menu: false,
      dates: [],
      topics: ["a","b","c","d","e"],
      topics_selected: [],
      authors: ["a1","b2","c3","d4","e5"],
      authors_selected: [],
      journals: ["a0","b0","c0","d0","e0","f0"],
      journals_selected: [],
      institutions: ["a01","b02","c03","d04"],
      institutions_selected: [],
    };
  },
  methods: {},
};
</script>

<style scoped>
.title {
  margin-left: 15px;
  font-size: 20;
  width: 5px;
  height: 30px;
}
.line {
  margin-left: 15px;
  position: relative;
  width: 300px;
  height: 1px;
  background-color: #d4d4d4;
  text-align: center;
  font-size: 16px;
  color: rgba(101, 101, 101, 1);
}
.datePicker {
  margin-left: 15px;
  height: 5%;
  width: 100px;
  align-content: center;
  font-size: 10%;
}
.checkboxLabel{
  margin-top: 10px;
  margin-bottom: 10px;
}
.checkboxItem{
  margin: 3px;
}
.checkbox{
  margin-left: 15px;
}
</style>