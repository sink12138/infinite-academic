<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <v-combobox
          label="Combobox"
          :value="input"
          :items="result"
          :search-input.sync="input"
          @update:search-input="getData"
          outlined
        ></v-combobox>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>

export default {
  data:() =>  ({
    input: '',
    result: ['dasd','123d'],
    count: 0
  }),
  methods:{
    getData() {
      this.result.push(this.count)
      this.count++
      console.log(this.input)
    }
  }
}

</script>


<style>

</style>
